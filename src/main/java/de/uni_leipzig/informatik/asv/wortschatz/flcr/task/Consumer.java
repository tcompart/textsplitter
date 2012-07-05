package de.uni_leipzig.informatik.asv.wortschatz.flcr.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Consumer<T> extends BasicListenerClass implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(Consumer.class);
	private static final AtomicInteger atomicNumber = new AtomicInteger(0);

	private final BlockingQueue<T> queue;
	private final Check<T> check;
	private final BlockingQueue<Boolean> resultQueue;

	public Consumer(final BlockingQueue<T> inputQueue) {
		this(inputQueue, null, null);
	}

	public Consumer(final BlockingQueue<T> inputQueue, final Check<T> inputCheck, BlockingQueue<Boolean> inputResultQueue) {
		if (inputQueue == null) { throw new NullPointerException(); }
		this.queue = inputQueue;
		this.check = inputCheck;
		this.resultQueue = inputResultQueue;

		// build the identifier with a string buffer because of memory and time
		// reasons: for more information read O'Reillys Hardcore Java (2004)
		final StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		sb.append("[");
		sb.append(atomicNumber.incrementAndGet());
		sb.append("]");
		this.identifier = sb.toString();

		if (log.isInfoEnabled()) {
			log.info(String.format("[%s]: newly created.", this.getIdentifier()));
			if (inputCheck == null) {
				log.info(String.format("%s: A validation of the results (Check) is disabled.", this.getIdentifier()));
			} else {
				log.info(String.format("%s: A validation of the results (Check) is enabled.", this.getIdentifier()));
			}
		}
	}

	private final String identifier;

	public String getIdentifier() {
		return this.identifier;
	}

	public abstract void consume(T t) throws InterruptedException;

	@Override
	public void run() {
		boolean interrupted = false;
		while (!Thread.interrupted()) {
			T t = null;
			boolean taken = false;
			try {
				t = this.queue.take();
				taken = (t != null && !this.queue.contains(t));
				if (taken) {
					log.debug(String.format("[%s]: consuming new taken object of queue.", this.getIdentifier()));
					this.consume(t);
					taken = !taken;
				} else {
					interrupted = true;
					Thread.sleep(1000);
				}
				/*
				 * the other case: !taken
				 * would mean, object t is null
				 * or object t is still in the queue....
				 * therefore the next object can be taken, because
				 * the queue offers still the same object
				 */
			} catch (InterruptedException ex) {
				interrupted = true;
			} finally {
				if (!interrupted && taken) {
					if (this.check != null) {
						try {
							this.check.validate(t);
							log.info(String.format(
									"[%s]: CHECK SUCCESSFUL using check of class '%s'", this.getIdentifier(),
									this.check.getClass().getSimpleName()));
						} catch (CheckFailedException ex) {
							log.error(String.format("[%s]: CHECK FAILED using check of class '%s'", this
									.getIdentifier(), this.check.getClass().getSimpleName()));
							this.returnObjectBackToQueue(t);
						}
					} else {
						log.info(String.format(
								"[%s]: check of object is disabled. Object passed therefore test.",
								this.getIdentifier()));
					}
					if (this.resultQueue != null)
						this.resultQueue.offer(true);
					
				} else if (interrupted && taken) {
					/*
					 * ! this is probably the most important aspect of the consumer
					 * class !
					 * ! DO NOT CHANGE THE FOLLOWING LINES, EXCEPT YOU KNOW WHAT YOU
					 * ARE DOING !
					 * 
					 * handling taken, but returned objects,
					 * which were removed from the queue,
					 * those have to be returned back to the queue
					 */
					this.returnObjectBackToQueue(t);
					taken = !taken;
					interrupted = false;
				}
			}
		}
	}

	private void returnObjectBackToQueue(T t) {
		log.info(String.format("[%s]: returning object back to queue.", this.getIdentifier()));
		
		while (!this.queue.offer(t) && !this.queue.contains(t)) {
			try {
				Thread.sleep(10); // better sleep because the queue may lag in
									// time...
			} catch (InterruptedException ex) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("[%s]: interrupted while ofering object to queue.", this.getIdentifier()));
				}
			}
		}
	}

}