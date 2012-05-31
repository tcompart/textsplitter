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
	
	@BeforeClass
	public static void reduceLoggingLevel() {
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.ERROR);
	}
	
	
	@Test(timeout=2000)
	public void stressHeapTest() {

		SelectorPool<Object> selectorPool = new DefaultTestSelectorPool() {

			private final AtomicInteger counter = new AtomicInteger(0);

			@Override
			protected String create() throws ReachedEndException {

				if (this.counter.incrementAndGet() % 10000 == 0) {
					System.out.println(String.format("Already %d calls to create", this.counter.get()));
				}
				// this test requires really big objects, otherwise this test runs forever...
				return "anObjectString_"+this.counter.get()+"_someother1234567890!!!???=````''''###;:;:;String";
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
		try {
			for (;;) {
				try {
					String obj = (String) selectorPool.acquire();
					maxBytes -= obj.getBytes().length; // - number of bytes of a
														// string
					selectorPool.release(obj);
					
					if (maxBytes <= 0) {
						break;
					}
				} catch (ReachedEndException ex) {
					break;
				}
			}
		} catch (OutOfMemoryError exception) {
			fail("Actually many many object should have been created without an out of memory. Therefore the objects are not released properly: "+ exception.toString());
		}
	}
}
