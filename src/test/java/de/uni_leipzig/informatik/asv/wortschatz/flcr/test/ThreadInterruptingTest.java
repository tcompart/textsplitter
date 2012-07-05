package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadInterruptingTest {

	private static final Logger log = LoggerFactory.getLogger(ThreadInterruptingTest.class);
	
	@Test
	public void test() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(1);

		executor.execute(new InterruptleableTest());

		Thread.sleep(100);

		executor.shutdownNow();

		log.info("Finished executor service by shutting it down.");
		
		Thread.sleep(100);
	}

	public class InterruptleableTest implements Runnable {

		private Long count = 0L;

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				log.debug("---TEST---{}----", this.count++);
			}
		}
	}
}
