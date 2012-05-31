package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

public class ThreadInterruptingTest {

	@Test
	public void test() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(1);

		executor.execute(new InterruptleableTest());

		Thread.sleep(100);

		executor.shutdownNow();

		System.out.println("Finished.");

		Thread.sleep(100);
	}

	public class InterruptleableTest implements Runnable {

		private Long count = 0L;

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				System.out.println(String.format("---TEST---%d----", this.count++));
			}
		}
	}
}
