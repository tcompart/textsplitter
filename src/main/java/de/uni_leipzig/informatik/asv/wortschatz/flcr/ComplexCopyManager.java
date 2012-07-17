package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Task;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public class ComplexCopyManager implements Runnable, CopyManager {

	private static final Logger log = LoggerFactory
			.getLogger(ComplexCopyManager.class);

	private static final AtomicInteger instanceCount = new AtomicInteger(0);

	private final int numberOfCores;
	private final ExecutorService executionService;
	private final BlockingQueue<File> inputQueue;
	private final MappingFactory mappingFactory;

	private final String instanceName;

	private volatile boolean stop;

	private CyclicBarrier barrier;

	private boolean success;

	private boolean runs;

	public ComplexCopyManager(final File inputDirectory, final Configurator inputConfigurator)
			throws FileNotFoundException {
		if (inputDirectory == null || inputConfigurator == null) {
			throw new NullPointerException();
		}
		if (!inputDirectory.exists()) {
			throw new FileNotFoundException(inputDirectory.getAbsolutePath());
		}
		if (!inputDirectory.isDirectory() || !inputDirectory.canRead()) {
			throw new IllegalArgumentException(String.format(
					"File '%s': is no directory or cannot be read.",
					inputDirectory.getAbsolutePath()));
		}
		this.numberOfCores = Runtime.getRuntime().availableProcessors();
		this.inputQueue = new ArrayBlockingQueue<File>(10, true,
				IOUtil.getFiles(inputDirectory, true));
		this.executionService = Executors.newSingleThreadExecutor();
		this.mappingFactory = new MappingFactory(inputConfigurator);
		this.instanceName = String.format("%s_%d", ComplexCopyManager.class.getSimpleName().toLowerCase(), instanceCount.incrementAndGet());
		
	}

	public void start() {

		if (this.isRunning()) {
			log.error("[{}]: Instance of class '{}' is still running, and cannot be started again.", this.getInstanceName(), ComplexCopyManager.class.getName());
			return;
		}
		assert this.isRunning() == false;
		assert this.isStoped() == true;
		
		this.runs = true;
		
		assert this.isRunning() == true;
		assert this.isStoped() == false;
		
		this.success = false;

		assert this.isSuccessful() == false;
		
		this.executionService.execute(this);
		this.executionService.shutdown();
		
		// wait until the barrier was initialized (done by thread)
		while (this.barrier == null) {
			synchronized (this) {
				try {
					log.debug("[{}]: Still waiting that the barrier gets initialized. Has not be done yet.", this.getInstanceName());
					this.wait(1000);
				} catch (InterruptedException e) {
					// ignore, because we wait for the barrier to return
				}
			}
		}
	}

	public boolean awaitTermination() throws InterruptedException,
			ExecutionException {
		return this.awaitTermination(0, TimeUnit.MILLISECONDS);
	}

	public boolean awaitTermination(long time, final TimeUnit timeUnit)
			throws InterruptedException, ExecutionException {
		if (!this.executionService.isShutdown() && this.barrier == null) {
			return false;
		}
		
		try {
			if (time <= 0) {
				this.barrier.await();
			} else {
				this.barrier.await(time, timeUnit);
			}
			if (!this.inputQueue.isEmpty()) {
				this.awaitTermination(time, timeUnit);
			}
		} catch (BrokenBarrierException ex) {
			log.error(
					"One or more threads have been broken free from the defined barrier, and therefore this exception occurred. That means, that the threads did not finish their jobs properly!!!!",
					ex);
			throw new ExecutionException(ex);
		} catch (TimeoutException e) {
			log.error(
					"The specified time '%s' elapsed, and the threads did not finish in the specified time, and had to be stoped forcefully.",
					String.format("%d %s", time, timeUnit.toString()));
			return false;
		}
		return true;
	}

	@Override
	public void run() {

		while (!this.inputQueue.isEmpty() && !Thread.interrupted() && !stop) {
			File file = null;
			try {
				file = inputQueue.poll(1, TimeUnit.SECONDS);
				if (file == null) {
					log.warn("The queue is empty, and still this Manager tried to pull next fill from queue. That should not be possible. Breaking the currently running loop now.");
					break;
				}
			} catch (InterruptedException ex) {
				log.warn(
						"Manager was interrupted while taking next file '{}' from queue, and dispatching the next task producers and consumers.",
						(file != null ? file.getName()
								: "null(not initialized file instance)"));
				continue; // continue with next file
			}

			try {

				final int numberOfThreads = numberOfCores + 1;

				barrier = new CyclicBarrier(numberOfThreads);
				final Textfile textfile = new Textfile(file);
				final MappingFactory mappingFactory = new MappingFactory(this.getConfigurator());
				final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<Task>();

				ExecutorService ecs = Executors
						.newFixedThreadPool(numberOfThreads);
				for (int i = 0; i < numberOfCores; i++) {
					ecs.execute(new TaskProducer(barrier, textfile,
							mappingFactory, taskQueue));
				}

				ecs.execute(new TaskConsumer(barrier, taskQueue));

				while (barrier.getParties() != barrier.getNumberWaiting())
					try {
						barrier.await();
						break;
					} catch (InterruptedException ex) {
						log.warn("Barrier was interrupted from its state. Continuing if not all parties have reached the same point of break.");
					}
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			} catch (BrokenBarrierException ex) {
				throw new RuntimeException(ex);
			}
		}

		if (this.inputQueue.isEmpty()) {
			this.success = true;
		}
		log.info("[{}]: Finished job.", this.getInstanceName());
		this.runs = false;
	}

	@Override
	public void stop() {
		this.stop = true;
	}

	@Override
	public boolean isRunning() {
		return this.runs;
	}

	@Override
	public boolean isStoped() {
		return !this.runs;
	}

	@Override
	public boolean isSuccessful() {
		return this.success;
	}

	@Override
	public String getInstanceName() {
		return this.instanceName;
	}

	@Override
	public Configurator getConfigurator() {
		return this.getMappingFactory().getConfigurator();
	}

	@Override
	public MappingFactory getMappingFactory() {
		return this.mappingFactory;
	}
}
