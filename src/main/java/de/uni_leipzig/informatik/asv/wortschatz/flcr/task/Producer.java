package de.uni_leipzig.informatik.asv.wortschatz.flcr.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.SelectorPool;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.impl.EventInfo;

public abstract class Producer<Input, Output> extends BasicListenerClass implements Runnable {

	private static final AtomicInteger atomicNumber = new AtomicInteger(0);

	private static final Logger log = LoggerFactory.getLogger(Producer.class);

	private final String identifier;

	private final SelectorPool<Input> pool;
	private final BlockingQueue<Output> queue;

	private int currentIn = 0;
	private int currentOut = 0;

	private volatile boolean reachedEnd = false;

	public Producer(final BlockingQueue<Output> inputQueue, final SelectorPool<Input> inputPool) {
		
		if (inputPool == null || inputQueue == null) { throw new NullPointerException(); }		
		
		this.queue = inputQueue;
		this.pool = inputPool;

		final StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		sb.append("[");
		sb.append(atomicNumber.incrementAndGet());
		sb.append("]");
		this.identifier = sb.toString();

		if (log.isInfoEnabled()) {
			log.info(String.format("[%s]: newly created.", this.getIdentifier()));
		}
	}

	public abstract Output produce(Input input) throws InterruptedException;

	public SelectorPool<Input> getPool() {
		return this.pool;
	}

	public BlockingQueue<Output> getQueue() {
		return this.queue;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public void run() {
		while (!this.finished() && !Thread.interrupted()) {
			Input in = null;
			Output out = null;
			try {
				in = this.getPool().acquire();
				if (in != null) {

					this.currentIn++;

					if (log.isDebugEnabled())
						log.debug(String.format(
								"[%s]: Aquired an object of class '%s' (nr: %d).", this.getIdentifier(), in
										.getClass().getSimpleName(), this.currentIn));

					out = this.produce(in);

					this.addToQueue(out);

					this.currentOut++;

					this.releaseFromPool(in);

					if (log.isDebugEnabled())
						log.debug(String.format(
								"[%s, nr: %d]: Produced an object of class '%s' from input object '%s' (nr: %d).", this
										.getIdentifier(), this.currentOut, out.getClass().getSimpleName(), in
										.getClass().getSimpleName(), this.currentIn));
				}
			} catch (InterruptedException ex) {
				if (log.isDebugEnabled()) {
					log.debug(String.format(
											"[%s]: Interrupted while producing object. Number of current input object: %d, number of current output object: %d",
											this.getIdentifier(), this.currentIn, this.currentOut));
				}
			} catch (ReachedEndException ex) {
				log.info(String.format(
										"[%s]: Reached the end of the input objects of class '%s' (nr: %d). Stopping production process.",
										this.getIdentifier(), this.getPool().getClass().getSimpleName(), this.currentIn));
				// TODO logging // event firing: this means every producer will
				// die at this point. Hopefully this breaks the loop
				this.finish();
			}
		}
	}

	private void addToQueue(final Output output) {
		log.debug(String.format("[%s]: Trying to put object of class '%s' into the producing queue.", this
				.getIdentifier(), output.getClass().getSimpleName()));

		
		try {
			while (!this.getQueue().offer(output, 100, TimeUnit.MILLISECONDS)) {
				synchronized (this) {
					this.wait(100);
				}
			}
		} catch (InterruptedException ex) {
			log.info(String.format("[%s]: was interrupted while offering an object to input queue. Trying again.",this.getIdentifier()));
			ex.printStackTrace();
		} finally {
			// this may fail, because the objects is taken before this point of code can be reached
			assert this.getQueue().contains(output);			
		}
	}

	private void releaseFromPool(Input input) {
		log.debug(String.format(
				"[%s]: Releasing the input object of class '%s' back to pool.", this.getIdentifier(), input
						.getClass().getSimpleName()));
		this.getPool().release(input);
	}

	private void finish() {
		if (!this.reachedEnd) {
			this.reachedEnd = !this.reachedEnd;
		}
	}

	private boolean finished() {
		return this.reachedEnd;
	}

}