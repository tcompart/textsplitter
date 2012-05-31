package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

import de.uni_leipzig.asv.clarin.common.tuple.Pair;

public abstract class SelectorPool<T> {

	private static final Logger log = LoggerFactory.getLogger(SelectorPool.class);

	private static final boolean DEBUG_ENABLED = log.isDebugEnabled();
	
	private static final boolean INFO_ENABLED = log.isInfoEnabled();
	
	private static final String DEFAULT_NAME = "DefaultSelectorPool";

	private static final long DEFAULT_EXPIRATIONTIME = 30000;

	private final long expirationTime;
	/**
	 * locked means objects, which are not published to any other instance, and
	 * therefore private for this object pool
	 */
	private final Hashtable<T, Long> lockedObjs;
	/**
	 * unlocked means objects, which are already published, and therefore into
	 * the wild
	 */
	private final Hashtable<T, Long> unlockedObjs;
	/**
	 * a possible object or instance filter
	 */
	private final Predicate<T> filter;

	private final AtomicInteger currentObjectNumber = new AtomicInteger(0); 

	private final String name;

	public SelectorPool(final String inputName) {
		this(inputName, null, null, DEFAULT_EXPIRATIONTIME);
	}

	public SelectorPool(final Predicate<T> inputFilter) {
		this(DEFAULT_NAME, inputFilter, null, DEFAULT_EXPIRATIONTIME);
	}

	public SelectorPool(final Predicate<T> inputFilter, final Integer maxSize) {
		this(DEFAULT_NAME, inputFilter, maxSize, DEFAULT_EXPIRATIONTIME);
	}

	public SelectorPool(final String inputName, final Predicate<T> inputFilter) {
		this(inputName, inputFilter, null, DEFAULT_EXPIRATIONTIME);
	}

	public SelectorPool(final Integer maxSize) {
		this(DEFAULT_NAME, null, maxSize, DEFAULT_EXPIRATIONTIME);
	}

	public SelectorPool(final String inputName, final Integer maxSize) {
		this(inputName, null, maxSize, DEFAULT_EXPIRATIONTIME);
	}

	public SelectorPool(final String inputName, final Predicate<T> inputFilter, final Integer maxSize,
			final long inputExpirationTime) {
		this.name = inputName;
		this.filter = inputFilter;

		if (maxSize == null) {
			this.unlockedObjs = new Hashtable<T, Long>();
			this.lockedObjs = new Hashtable<T, Long>();
		} else {
			this.unlockedObjs = new Hashtable<T, Long>(maxSize);
			this.lockedObjs = new Hashtable<T, Long>(maxSize);
		}
		this.expirationTime = inputExpirationTime;

	}

	protected abstract T create() throws ReachedEndException, InterruptedException;

	public abstract int size();

	public abstract boolean validate(final T obj);

	protected abstract void releaseObj(final T obj);

	public void release(final T obj) {

		if (obj == null) { throw new NullPointerException(); }

		if (DEBUG_ENABLED)
			log.info(String.format("[%s]: releasing object '%s'", this.getInstanceName(), obj.toString()));

		this.lockedObjs.remove(obj);
		this.unlockedObjs.remove(obj);

		this.releaseObj(obj);

	}

	public int currentNumber() {
		return this.currentObjectNumber.intValue();
	}

	public T acquire() throws ReachedEndException {

		Long now = System.nanoTime();

		T t = null;
		if (this.hasNext(now)) {
			t = this.getNext();
		} else {
			for (;; t = null) {
				try {
					t = this.create();
					if (!this.validate(t)) {
						if (INFO_ENABLED)
							log.info(String.format("[%s]: Failed validation for object '%s'. Proceeding with creating an new object.",this.getInstanceName(), t.toString()));
						continue;
					}
				
					if (this.filter != null && !this.filter.apply(t)) {
						if (INFO_ENABLED)
							log.info(String.format("[%s]: Filter does not allow object '%s'. Proceeding with creating an new object.",this.getInstanceName(), t.toString()));
						continue;
					}
					
					if (DEBUG_ENABLED)
						log.debug(String.format("[%s]: Unlocking object '%s'. Keeps present until it gets released.", this.getInstanceName(),t.toString()));
					
					assert t != null;
					assert now != null;
					
					this.unlockedObjs.put(t, now);
					break;	
				} catch (InterruptedException ex) {
					// the #create() method is a blocking method, which can
					// be interrupted
					log.warn(String.format("[%s]: Interupted while creating an new object.", this.getInstanceName()));
				}
			}
		}

		/*
		 * this should be the only point of exit of this function,
		 * because the counter has to increase
		 */
		this.currentObjectNumber.incrementAndGet();
		
		return t;
	}

	protected String getInstanceName() {
		if (this.getClass().isAnonymousClass())
			return "InnerAnonymousClass";
			else if (this.name != null)
				return this.name;
			return this.getClass().getSimpleName();
	}

	public boolean finished() {
		return this.currentNumber() >= this.size();
	}

	/**
	 * @return
	 */
	private boolean hasNext(final Long time) {
		if (this.lockedObjs.size() > 0) {
			for (T obj: this.lockedObjs.keySet()) {
				long createdTime = this.lockedObjs.get(obj);
				if (time - createdTime > this.expirationTime && this.validate(obj)) {
					this.setNext(obj, time);
					return true;
				}
				// else remove and release the found objs (expired objs)
				// this can be trusted, because objects were not published yet
				this.lockedObjs.remove(obj);
				this.release(obj);
			}
		}
		return false;
	}

	private T next;

	/**
	 * @param obj
	 */
	private void setNext(final T obj, final long time) {

		if (obj == null) { throw new NullPointerException(); }

		this.lockedObjs.remove(obj);
		this.unlockedObjs.put(obj, time);
		this.next = obj;
	}

	/**
	 * @return
	 */
	private T getNext() {
		return this.next;
	}

}
