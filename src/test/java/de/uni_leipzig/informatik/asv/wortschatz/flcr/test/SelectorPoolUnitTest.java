package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Predicate;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.SelectorPool;

public class SelectorPoolUnitTest {

	public final static Integer maximumNumber = 10;

	private SelectorPool<Object> selectorPool;

	@Before
	public void setupClass() {
		this.selectorPool = new DefaultTestSelectorPool() {

			@Override
			public boolean validate(Object inputObj) {
				return inputObj != null;
			}
		};
	}

	@Test
	public void create() {

		assertThat(this.selectorPool, notNullValue());

		assertThat(this.selectorPool.currentNumber(), is(0));
		assertThat(this.selectorPool.size(), is(maximumNumber));
		assertThat(this.selectorPool.size() > this.selectorPool.currentNumber(), is(true));

		// default validation: every object is valid, which is not null
		assertThat(this.selectorPool.validate(null), is(false));
		assertThat(this.selectorPool.validate(new Object()), is(true));

		assertThat(this.selectorPool.finished(), is(false));

	}

	@Test
	public void acquireObjects() {

		assertThat(this.selectorPool.finished(), is(false));

		for (int count = 0; count < maximumNumber; count++) {
			try {
				assertThat(this.selectorPool.acquire(), notNullValue());
			} catch (ReachedEndException ex) {
				fail(String.format("Reached already end of object pool: %s", ex.getMessage()));
			}
		}

		assertThat(this.selectorPool.finished(), is(true));

		try {
			this.selectorPool.acquire();
			fail("The maximum number of objects has to be reached by now.");
		} catch (ReachedEndException ex) {
			// expected exception
			assertThat(ex, notNullValue());
		}

	}

	@Test(expected = ReachedEndException.class)
	public void validateObjects() throws ReachedEndException {
		/*
		 * this test uses the validate(Object) method of the selector pool.
		 * creating 1000 objects, which are not valid: ReachedEndException
		 */
		this.selectorPool = new DefaultTestSelectorPool() {

			@Override
			public boolean validate(Object inputObj) {
				// every validation will fail: this means a new object will be
				// created again
				assertThat(inputObj, notNullValue());

				return false;
			}
		};
		// exception has to be thrown with the first call (maximumNumber of
		// objects are created, but none is valid)
		assertThat(this.selectorPool.acquire(), nullValue());
		fail("The reached end exception had to be thrown already... but has not.");
	}

	@Test(expected = ReachedEndException.class)
	public void filterObjects() throws ReachedEndException {
		/*
		 * this test uses the Filter.apply(Object) parameter of the selector
		 * pool.
		 * creating 1000 objects, which are all filtered: ReachedEndException
		 */
		this.selectorPool = new DefaultTestSelectorPool(new Predicate<Object>() {

			@Override
			public boolean apply(Object inputObj) {
				// this filter fails every object, which is not null...
				assertThat(inputObj, notNullValue());

				return inputObj == null;
			}
		}) {

			@Override
			public boolean validate(Object obj) {
				// every validation will be successful
				return true;
			}

		};
		// exception has to be thrown with the first call (maximumNumber of
		// objects are created, but none is valid)
		assertThat(this.selectorPool.acquire(), nullValue());
		fail("The reached end exception had to be thrown already... but has not.");
	}



	@Ignore("currently no idea how to test expired objects...")
	@Test
	public void expireObject() {
		// not really testable
	}

	public static abstract class DefaultTestSelectorPool extends SelectorPool<Object> {

		private final AtomicInteger counter = new AtomicInteger(0);

		public DefaultTestSelectorPool() {
			this(null);
		}

		public DefaultTestSelectorPool(final Predicate<Object> filter) {
			super(filter, 10);
		}

		@Override
		protected Object create() throws ReachedEndException {
			if (this.counter.getAndIncrement() >= maximumNumber) { throw new ReachedEndException(); }

			assert this.counter.get() <= maximumNumber;

			return new Object();
		}

		@Override
		protected void releaseObj(final Object obj) {
			final int releaseCount = this.counter.getAndDecrement();
			assert releaseCount == 0;
		}

		@Override
		public int size() {
			return maximumNumber;
		}

	}

}
