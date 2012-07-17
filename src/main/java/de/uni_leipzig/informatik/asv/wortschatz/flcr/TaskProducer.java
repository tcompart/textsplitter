package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.FileNotFoundException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Task;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;

public class TaskProducer implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(TaskProducer.class);
	private static final AtomicInteger instanceCount = new AtomicInteger(0);
	
	private final Textfile textfile;
	private final MappingFactory mappingFactory ;
	private final BlockingQueue<Task> outputQueue;
	private final CyclicBarrier barrier;
	private final String instanceName;
	
	public TaskProducer(CyclicBarrier inputBarrier, final Textfile inputTextfile, final MappingFactory inputMappingFactory, final BlockingQueue<Task> outputQueue) {
		if (inputTextfile == null || inputMappingFactory == null) { throw new NullPointerException(); }
		if (inputBarrier == null) { throw new NullPointerException(); }
		this.textfile = inputTextfile;
		this.mappingFactory = inputMappingFactory;
		this.outputQueue = outputQueue;
		this.barrier = inputBarrier;
		this.instanceName = String.format("%s_%d", TaskProducer.class.getSimpleName().toLowerCase(), instanceCount.incrementAndGet());
	}

	@Override
	public void run() {
		Source source = null;
		try {
			while ((source = textfile.getNext()) != null) {
				log.info("[{}]: Producing next task of source '{}'", this.getInstanceName(), source);
				Task task = new Task(new CopyCommand(source, mappingFactory.getSourceDomainMapping(textfile, source)));
				while (!outputQueue.offer(task)) {
					final Thread thread = Thread.currentThread();
					synchronized (thread) {
						try {
							thread.wait(1000);
						} catch (InterruptedException e) {
							log.warn("[{}]: Interrupted while waiting to add task {} to blocking queue.", this.getInstanceName(), task.getUniqueIdentifier());
						}
					}
				}
				log.info("[{}]: Offered task {} to blocking queue, and will resume with next task.", this.getInstanceName(), task.getUniqueIdentifier());
			}
		} catch (ReachedEndException e) {
			log.info("[{}]: No more task producable because {} '{}' has no more {}s found ({}).", new Object[]{ this.getInstanceName(), Textfile.class.getSimpleName(), textfile.toString(), Source.class.getSimpleName(), ReachedEndException.class.getSimpleName() });
			
			try {
			while (this.barrier.getNumberWaiting() < this.barrier.getParties())
				try {
					this.barrier.await();
					break;
				} catch (InterruptedException ex) {
					log.warn("[{}]: The inner barrier was interrupted, while waiting for other threads to reach this point of break. Continuing until every thread reached this point.", this.getInstanceName());
				}
			} catch (BrokenBarrierException ex) {
				String textfileName;
				try {
					textfileName = textfile.getFile().getAbsolutePath();
				} catch (FileNotFoundException e1) {
					textfileName = textfile.toString();
				}
				throw new RuntimeException(String.format("[%s]: The barrier was broken! That means, that one thread did something very stupid, and a lot of data got corrupted just now; last textfile worked on '%s' (Source '%s')", this.getInstanceName(), textfileName, source.toString()), ex);
			}
		}
		log.info("[{}]: Finished its job, and stops producing.", this.getInstanceName());
	}

	public String getInstanceName() {
		return this.instanceName;
	}

}