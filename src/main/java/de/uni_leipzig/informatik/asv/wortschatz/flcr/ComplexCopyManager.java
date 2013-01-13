package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public class ComplexCopyManager implements CopyManager {

	public static final int MAXIMUM_TASK_NUMBER_LIMIT = 1000;
	
	private static final Logger log = LoggerFactory
			.getLogger(ComplexCopyManager.class);

	private static final AtomicInteger instanceCount = new AtomicInteger(0);

	private static final int DEFAULT_NUMBER_OF_THREAD_PAIRS = 2;

	private final Set<Stoppable> threads = new HashSet<Stoppable>();
	private CyclicBarrier barrier;
	private final int numberOfParallelWorkers;
	private final ExecutorService executionService;;

	private final String instanceName;
	private final BlockingQueue<File> inputQueue;
	private final MappingFactory mappingFactory;

	private PrintStream out;

	public ComplexCopyManager(File parentFile, Configurator configurator) throws FileNotFoundException {
		this(parentFile, configurator, DEFAULT_NUMBER_OF_THREAD_PAIRS);
	}

	public ComplexCopyManager(final File inputDirectory, final Configurator inputConfigurator, final int inputNumberOfThreads)
			throws FileNotFoundException {
		if (inputDirectory == null || inputConfigurator == null) {
			throw new NullPointerException();
		}
		if (inputNumberOfThreads < 0) {
			throw new IllegalArgumentException("The assigned thread number should be between '0 - n' (n should not be bigger than 5, otherwise the threads maybe slower than a single-thread application))");
		}
		if (!inputDirectory.exists()) {
			throw new FileNotFoundException(inputDirectory.getAbsolutePath());
		}
		// trying to allow directory or file without any restrictions other than readability
		if (!inputDirectory.canRead()) {
			throw new IllegalArgumentException(String.format(
					"File '%s': cannot be read.",
					inputDirectory.getAbsolutePath()));
		}
		this.instanceName = String.format("%s_%d", ComplexCopyManager.class.getSimpleName().toLowerCase(), instanceCount.incrementAndGet());
		final Collection<File> files = IOUtil.getFiles(inputDirectory, true);
		this.inputQueue = new ArrayBlockingQueue<File>(files.size(), true,
				files);
		this.numberOfParallelWorkers = inputNumberOfThreads;
		this.executionService = Executors.newFixedThreadPool(2*numberOfParallelWorkers+1);
		this.mappingFactory = new MappingFactory(inputConfigurator);
		
	}

	public void start() {

		if (this.isRunning()) {
			log.error("[{}]: Instance of class '{}' is still running, and cannot be started again.", this.getInstanceName(), ComplexCopyManager.class.getName());
			return;
		}

		barrier = new CyclicBarrier(numberOfParallelWorkers*2+1);
		final MappingFactory mappingFactory = this.getMappingFactory();
		// maximum 1000 taks queue...
		final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<Task>(MAXIMUM_TASK_NUMBER_LIMIT);
		
		if (this.hasOutputStream())
			this.writeOutput(out, "Starting to query file queue. Number of entries: "+inputQueue.size());
		
		for (int i = 0; i < numberOfParallelWorkers; i++) {
			final TaskProducer producer = new TaskProducer(barrier, inputQueue, mappingFactory, taskQueue);
			if (this.hasOutputStream())
				producer.setOutputStream(out);
			threads.add(producer);
			executionService.execute(producer);
		}
		
		for (int i = 0; i < numberOfParallelWorkers; i++) {
			final TaskConsumer consumer = new TaskConsumer(barrier, taskQueue);
			threads.add(consumer);
			executionService.execute(consumer);
		}
		
		this.executionService.shutdown();
		
	}

	public boolean awaitTermination() throws InterruptedException,
			ExecutionException {
		return this.awaitTermination(0, TimeUnit.MILLISECONDS);
	}

	public boolean awaitTermination(long time, final TimeUnit timeUnit)
			throws InterruptedException, ExecutionException {
		if (!this.executionService.isShutdown() || this.barrier == null) {
			return false;
		}
		
		try {
			if (time <= 0) {
				this.barrier.await();
			} else {
				this.barrier.await(time, timeUnit);
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
	public void stop() {
		for (Stoppable t : threads) {
			t.stop();
		}
	}

	@Override
	public boolean isRunning() {
		
		if (this.barrier == null) {
			return false;
		}
		
		for (Stoppable t : threads) {
			if (!t.isStopped()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isStopped() {
		return !isRunning();
	}

	@Override
	public boolean isSuccessful() {
		return this.inputQueue.isEmpty();
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

	@Override
	public boolean hasModule(Module<?> module) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addModule(Module<?> module) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeModule(Module<?> module) {
		// TODO Auto-generated method stub
		
	}
	
	public void writeOutput(final PrintStream out, final String msg) {
		out.println(msg);
	}

	public boolean hasOutputStream() {
		return this.out != null;
	}
	
	@Override
	public void setOutputStream(PrintStream inputOut) {
		if (inputOut == null) { throw new NullPointerException(); }
		this.out = inputOut;
	}
}
