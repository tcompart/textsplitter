package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Task;

public class TaskConsumer implements Runnable, Stoppable {

	private static final Logger log = LoggerFactory.getLogger(TaskConsumer.class);
	
	private static final AtomicInteger instanceCount = new AtomicInteger(0);
	
	private final BlockingQueue<Task> taskQueue;

	private volatile boolean stop;
	
	private boolean isStopped = false;
	
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
		Task task;
		try {
			while (!stop && !Thread.interrupted()) {
				try {
					log.debug("[{}]: Taking next task of queue.",
							this.getInstanceName());

					assert this.taskQueue.size() <= ComplexCopyManager.MAXIMUM_TASK_NUMBER_LIMIT : "the task queue limit was broken! More tasks then expected an assigned were placed into the queue.";

					task = this.taskQueue.poll(1, TimeUnit.SECONDS);
					if (task == null) {
						log.debug("[{}]: Waiting at the barrier for the final end.", this.getInstanceName());
						this.barrier.await();
						break;
					}
					task.doTask();
					log.info("Finished task: {}", task.getUniqueIdentifier());
				} catch (InterruptedException ex) {
					log.info("Interrupted while taking next task from blocking queue.");
				}
			}
		} catch (BrokenBarrierException ex) {
			throw new RuntimeException(String.format("[%s]: broken free of barrier. Please check the logs for more details.", this.getInstanceName()), ex);
		} finally {
			log.info("[{}]: Bye.", this.getInstanceName());
			isStopped = true;
		}
	}

	public String getInstanceName() {
		return this.instanceName;
	}

	public boolean isStopped() {
		return isStopped;
	}
	
	public synchronized void stop() {
		this.stop = true;
	}
	
}