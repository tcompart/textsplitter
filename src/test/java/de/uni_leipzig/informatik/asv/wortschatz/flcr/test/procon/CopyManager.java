package de.uni_leipzig.informatik.asv.wortschatz.flcr.test.procon;

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
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.CopyController.CopyCommand;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Task;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.SelectorPool;

/**
 * 
 * CopyManager Solution0:
 * 
 * 	jeder Producer eine Textfile
 *  eine Menge von Consumer working für alle Producer: nur abhängig von einer Queue
 * 
 * @author Torsten Compart
 *
 */
public class CopyManager {

	private static final Logger log = LoggerFactory.getLogger(CopyManager.class);
	
	private final int numberOfCores;
	private final TextfilePreLoader preloader;

	private CyclicBarrier barrier;
	
	public CopyManager(final File inputDirectory) throws FileNotFoundException {
		if (inputDirectory == null) { throw new NullPointerException(); }
		if (!inputDirectory.exists()) { throw new FileNotFoundException(inputDirectory.getAbsolutePath()); }
		if (!inputDirectory.isDirectory() || !inputDirectory.canRead()) { throw new IllegalArgumentException(String.format("File '%s': is no directory or cannot be read.", inputDirectory.getAbsolutePath())); }
		this.preloader = new TextfilePreLoader(new ArrayBlockingQueue<File>(10, true, IOUtil.getFiles(inputDirectory, true)));
		this.numberOfCores = Runtime.getRuntime().availableProcessors();
	}

	public void start() {
		
		if (this.barrier != null) throw new IllegalStateException(String.format("Instance of class %s has to be finished first", this.barrier.getClass().getSimpleName()));
		
		final int numberOfProducers;
		if (numberOfCores > 2) 
			numberOfProducers = 2;
		else
			numberOfProducers = 1;
		
		// this means, that the barrier executes a new producer, if any producer finishes
		this.barrier = new CyclicBarrier(1);
		new DispatcherWorker(numberOfProducers, preloader);
		
		
		/*
		 * manager: input eine menge von dateien
		 * jede datei: eine textfile
		 * jede textfile: eine menge von sources
		 * 
		 * producer: nimmt textfile, erzeugt menge von task
		 * consumer: nimmt queue von task
		 * 
		 * 	anzahl von producers?
		 *  create barrier
		 *  dispatch number of producers (for one file each)
		 *  until no files exit anymore
		 */
		
	}
	
	public boolean awaitTermination() throws InterruptedException, ExecutionException {
		return this.awaitTermination(0, TimeUnit.MILLISECONDS);
	}
	
	public boolean awaitTermination(long time, final TimeUnit timeUnit) throws InterruptedException, ExecutionException {
		if (this.barrier == null) {
			return false;
		}
		
		try {
			while (!this.preloader.isFinished()) {
				if (time <= 0) {
					this.barrier.await();
				} else {
					this.barrier.await(time, timeUnit);
				}
			}
			this.barrier = null;
		} catch (BrokenBarrierException ex) {
			log.error("One or more threads have been broken free from the defined barrier, and therefore this exception occurred. That means, that the threads did not finish their jobs properly!!!!",ex);
			throw new ExecutionException(ex);
		} catch (TimeoutException e) {
			log.error("The specified time '%s' elapsed, and the threads did not finish in the specified time, and had to be stoped forcefully.", String.format("%d %s", time, timeUnit.toString()));
			return false;
		}
		return true;
	}
	
	public static class DispatcherWorker implements Runnable {

		public DispatcherWorker(final int numberOfProducers, final SelectorPool<Textfile> preloader) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class TextfilePreLoader extends SelectorPool<Textfile> {
	
		private final BlockingQueue<File> queue;
	
		public TextfilePreLoader(final BlockingQueue<File> inputQueue) {
			super("textfilePreloader");
			if (inputQueue == null) { throw new NullPointerException(); }
			this.queue = inputQueue;
		}
	
		// TODO preloaded file!!!
		private Textfile preloaded;
		
		@Override
		protected Textfile create() throws ReachedEndException, InterruptedException {
			final Textfile textfile;
			try {
				final File file = queue.poll(1, TimeUnit.SECONDS);
				if (file == null) { throw new ReachedEndException(); }
				textfile = new Textfile(file);
				
			} catch (IOException ex) {
				throw new RuntimeException(String.format("The instance of class '%s' could not be initialized.", Textfile.class.getSimpleName()),ex);
			}
			
			return textfile;
		}
	
		@Override
		public int size() {
			return this.queue.size();
		}
	
		@Override
		public boolean validate(Textfile obj) {
			try {
				return (obj != null && obj.getFile() != null);
			} catch (FileNotFoundException e) {
				return false;
			}
		}
	
		@Override
		protected void releaseObj(Textfile obj) {
			this.queue.remove(obj);
		}
		
	}


	public static class TestProducer implements Runnable {

		private static final Logger log = LoggerFactory.getLogger(TestConsumer.class);
		
		private static final AtomicInteger taskCount = new AtomicInteger(0);
		
		private final BlockingQueue<Task> resultQueue;

		private final BlockingQueue<File> queue;

		private final CyclicBarrier barrier;

		public TestProducer(final int numberOfConsumers, final BlockingQueue<File> inputQueue) {
			if (numberOfConsumers <= 0) { throw new IllegalArgumentException(); }
			if (inputQueue == null) { throw new NullPointerException(); }
			log.info("Initialized new Producer: "+this.toString());
			this.queue = inputQueue;
			this.barrier = new CyclicBarrier(numberOfConsumers, new TestProducer(numberOfConsumers, inputQueue));
			this.resultQueue = new LinkedBlockingQueue<Task>();
		}
		
		@Override
		public void run() {
						
			File file = null;
			try {
				while ((file = queue.poll(1, TimeUnit.SECONDS)) != null && !Thread.interrupted()) {
					log.info("Trying to take new file for producing new tasks.");
					
					log.info("Initializing new Textfile instance with taken file '{}'", file.getName());
					
					Textfile textfile;
					try {
						textfile = new Textfile(file);
					} catch (IOException ex) {
						throw new RuntimeException("Execution exception",ex);
					}
					Source source = null;
					MappingFactory mappingFactory = new MappingFactory();
					try {
						while ((source = textfile.getNext()) != null) {
							final File outputfile = mappingFactory.getSourceDomainMapping(textfile, source);
							log.info("Initializing new task (nr: {}) by file '{}', with output file '{}': source '{}'", new Object[]{ taskCount.incrementAndGet(), file.getName(), outputfile.getName(), source.toString()});
							this.resultQueue.offer(new Task(new CopyCommand(source, outputfile)));
							log.info("Offered successfully task (nr: {}) to {}", taskCount.get(), BlockingQueue.class.getSimpleName());
						}
					} catch (ReachedEndException e) {
						log.info("Reach the final end of the production cycle. Finishing everything up, and continuing with next file.");
					}
					
					try {
						barrier.await();
					} catch (BrokenBarrierException ex) {
						log.error("Critical exception. While awaiting the end of all threads for this barrier, some thread died before it could reach the barrier, and therefore some work was not done. Length of task queue is '%d', number of files ",ex);
					}
				}
			} catch (InterruptedException ex) {
				log.info("Interrupted while waiting for next input file. Dying here, and maybe the next thread is more successful.");
			}
			
			if (file == null) {
				log.info("Inputqueue of files is empty, and therefore nothing to do anymore.");
			} else {			
				log.info("Producer is dying a horrible and final!!!! death.");
			}
		}
		
	}
	
	
	public static class TestConsumer implements Runnable {

		private static final Logger log = LoggerFactory.getLogger(TestConsumer.class);
		
		private final BlockingQueue<Task> queue;

		private volatile boolean stop;

		public TestConsumer(final BlockingQueue<Task> inputTaskQueue) {
			if (inputTaskQueue == null) { throw new NullPointerException(); }
			log.info("Initialized new Consumer: "+this.toString());
			this.queue = inputTaskQueue;
		}
		
		@Override
		public void run() {
			while (!isStoped() && !Thread.interrupted()) {
				log.info("Trying to access next task of queue");
				Task task;
				try {
					task = queue.poll(1, TimeUnit.SECONDS);
					if (task == null) {
						log.debug("Queue is empty. This thread waits 1000 milliseconds until the next try.");
						synchronized (this) {
							this.wait(1000);
						}
						continue;
					}
				} catch (InterruptedException e) {
					log.warn("Interrupted while waiting for a new task. Continue loop.");
					continue;
				}
				
				log.info("Executing taken task '{}'", task.getUniqueIdentifier());
				task.doTask();
				
				log.info("Task {} released! (task finished: {}, task succesful: {})", new Object[]{ task.getUniqueIdentifier(), task.finished(), task.successful()});
			}
		}

		private boolean isStoped() {
			return stop;
		}
		
		public void stop() {
			this.stop = true;
		}
		
	}

	public static void main(String... args) throws FileNotFoundException, ExecutionException, InterruptedException {
		CopyManager manager = new CopyManager(new File("Unigramm"));
		manager.start();
		assert manager.awaitTermination() == true;
	}

}
