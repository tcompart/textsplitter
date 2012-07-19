package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Task;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;

public class TaskProducer implements Runnable, Stoppable {

	private static final Logger log = LoggerFactory.getLogger(TaskProducer.class);
	private static final AtomicInteger instanceCount = new AtomicInteger(0);
	
	private final BlockingQueue<File> fileQueue;
	private final MappingFactory mappingFactory ;
	private final BlockingQueue<Task> outputQueue;
	private final CyclicBarrier barrier;
	private final String instanceName;
	private volatile boolean stop;
	
	private boolean isStoped = false;
	private PrintStream out;
	
	public TaskProducer(CyclicBarrier inputBarrier, final BlockingQueue<File> inputQueue, final MappingFactory inputMappingFactory, final BlockingQueue<Task> outputQueue) {
		if (inputQueue == null || inputMappingFactory == null) { throw new NullPointerException(); }
		if (inputBarrier == null) { throw new NullPointerException(); }
		this.fileQueue = inputQueue;
		this.mappingFactory = inputMappingFactory;
		this.outputQueue = outputQueue;
		this.barrier = inputBarrier;
		this.instanceName = String.format("%s_%d", TaskProducer.class.getSimpleName().toLowerCase(), instanceCount.incrementAndGet());
	}

	@Override
	public void run() {
		File file = null;
		try {
			try {
				while (!this.fileQueue.isEmpty() && !stop) {
					// 1. GET FILE
					try {
						file = this.fileQueue.poll(1, TimeUnit.SECONDS);
					} catch (InterruptedException ex) {
						log.warn("[{}]: Interruption while pulling file from queue.", this.getInstanceName());
						if (file == null) {
							continue;
						}
					}
					// 2. CHECK FILE
					if (file == null) {
						log.info("[{}]: The file queue is empty now.", this.getInstanceName());
						break;
					}
					// 3. PRODUCE TEXTFILE
					Textfile textfile = new Textfile(file);
					
					if (this.hasOutputStream()) {
						this.writeOutput(out, "Took textfile '"+file.getAbsolutePath()+"'");
					}
					
					Source source = null;
					try {
						// 4. FOR EVERY SOURCE OF TEXTFILE
						while ((source = textfile.getNext()) != null) {
							log.info("[{}]: Producing next task of source '{}'", this.getInstanceName(), source);
							Task task = new Task(new CopyCommand(source, mappingFactory.getSourceDomainMapping(textfile, source)));
							// 5. OFFER TASK TO QUEUE
							while (!this.outputQueue.offer(task)) {
								// WAIT OTHERWISE
								final Thread thread = Thread.currentThread();
								synchronized (thread) {
									try {
										thread.wait(1000);
									} catch (InterruptedException e) {
										log.warn("[{}]: Interrupted while waiting to add task {} to blocking queue.", this.getInstanceName(), task.getUniqueIdentifier());
									}
								}
							}
							log.debug("[{}]: Offered task {} to blocking queue, and will resume with next task.", this.getInstanceName(), task.getUniqueIdentifier());
						}
					} catch (ReachedEndException e) {
						log.debug("[{}]: Finished producing tasks for file '{}'. May proceed with next one if accessible.", this.getInstanceName(), file.getAbsolutePath());
					}
				}
				// this point should mark, that everything worked successfully...
				/*
				 * this is also the only point of exit, therefore if no exception is thrown
				 * then the thread should be successful...
				 */
			} finally {
				while (this.barrier.getNumberWaiting() < this.barrier.getParties()) {
					try {
						log.debug("[{}]: Waiting at the barrier for the final end.", this.getInstanceName());
						this.barrier.await();
						break;
					} catch (InterruptedException ex) {
						log.warn("[{}]: Interruption while waiting for the global barrier. Returning back to barrier to await the end of production.", this.getInstanceName());
					}
				}
			}
		} catch (Exception ex) {
			if (ex instanceof RuntimeException) { throw (RuntimeException) ex; }
			throw new RuntimeException(String.format("Critical error while producing tasks. This thread '%s' may die horribly because of file '%s'", this.getInstanceName(), (file != null ? file.getAbsolutePath() : "not-initialized!!!")));
		} finally {
			log.info("[{}]: Bye!", this.getInstanceName());
			isStoped = true;
		}
	}

	public String getInstanceName() {
		return this.instanceName;
	}

	@Override
	public void stop() {
		this.stop = true;
	}

	@Override
	public boolean isStoped() {
		return this.isStoped;
	}

	
	public void writeOutput(final PrintStream out, final String msg) {
		out.println(msg);
	}

	public boolean hasOutputStream() {
		return this.out != null;
	}
	
	public void setOutputStream(final PrintStream inputOut) {
		if (inputOut == null) { throw new NullPointerException(); }
		this.out = inputOut;
	}

}