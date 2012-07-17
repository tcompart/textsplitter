package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Task;

public class TaskConsumer implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(TaskConsumer.class);
	
	private static final AtomicInteger instanceCount = new AtomicInteger(0);
	
	private final BlockingQueue<Task> taskQueue;

	private volatile boolean stop;
	
	private final CyclicBarrier barrier;

	private final String instanceName;
	
	public TaskConsumer(final CyclicBarrier inputBarrier, final BlockingQueue<Task> inputTaskQueue) {
		if (inputTaskQueue == null) { throw new NullPointerException(); }
		if (inputBarrier == null) { throw new NullPointerException(); }
		this.taskQueue = inputTaskQueue;
		this.barrier = inputBarrier;
		this.instanceName = String.format("%s_%d", TaskConsumer.class.getSimpleName().toLowerCase(), instanceCount.incrementAndGet());
	}

	@Override
	public void run() {
		while (!isStoped() && !Thread.interrupted()) {
			try {
				log.info("[{}]: Taking next task of queue.", this.getInstanceName());
				
				assert this.taskQueue.size() < 100;
				
				final Task task = this.taskQueue.take();
				if (task == null) { this.barrier.await(); }
				task.doTask();
				log.info("Finished task: {}", task.getUniqueIdentifier());
			} catch (InterruptedException ex) {
				log.info("Interrupted while taking next task from blocking queue.");
			} catch (BrokenBarrierException ex) {
				//TODO
			}
			
		}
	}

	public String getInstanceName() {
		return this.instanceName;
	}

	private boolean isStoped() {
		return stop;
	}
	
	public synchronized void stop() {
		this.stop = true;
	}
	
}