package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.SelectorPool;

public class SelectorPoolIntegrationTest extends SelectorPoolUnitTest {
	
	private static final Logger log = LoggerFactory.getLogger(SelectorPoolIntegrationTest.class);
	
	@BeforeClass
	public static void reduceLoggingLevel() {
		// have to reduce the logging level, otherwise the test gets a timeout
		org.apache.log4j.Logger.getLogger(SelectorPool.class).setLevel(Level.WARN);
	}
	
	
	@Test(timeout=20000)
	public void stressHeapTest() {
		SelectorPool<Object> selectorPool = new DefaultTestSelectorPool() {

			private final AtomicInteger counter = new AtomicInteger(0);

			@Override
			protected String create() throws ReachedEndException {

				if (this.counter.incrementAndGet() % 10000 == 0) {
					log.debug("Already {} calls to create", this.counter.get());
				}
				return String.format("anObjectString_%d_someother1234567890!!!???=````''''###;:;:;String", this.counter.get());
			}

			@Override
			protected void releaseObj(final Object inputObj) {
				assertThat(inputObj, notNullValue());
			}

			@Override
			public boolean validate(Object inputObj) {
				return inputObj != null;
			}
		};

		// get the number of total bytes (a part of this memory should be the
		// heap space...)
		long maxBytes = Runtime.getRuntime().totalMemory();
		log.info("Creating many many objects and destroying them for stressing the heap space a little.");
		try {
			for (;;) {
				try {
					String obj = (String) selectorPool.acquire();
					maxBytes -= obj.getBytes().length; // - number of bytes of a
														// string
					selectorPool.release(obj);
					
					if (maxBytes <= 0) {
						log.info("Reached the end of the heap space memory, or created as many objects as the size of the assigned heap space.");
						break;
					}
				} catch (ReachedEndException ex) {
					log.info("Reached end of production cycle without reaching the required number of objects for bursting the heap space.");
					break;
				}
			}
		} catch (OutOfMemoryError exception) {
			fail("Actually many many object should have been created without an out of memory. Therefore the objects are not released properly: "+ exception.toString());
		}
	}
}
