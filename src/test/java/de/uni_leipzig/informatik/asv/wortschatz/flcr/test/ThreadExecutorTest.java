package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadExecutorTest {

	public static final Logger log = LoggerFactory.getLogger(ThreadExecutorTest.class);

	@BeforeClass
	public static void setUpClass() {
		org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
		root.setLevel(Level.INFO);
	}

	@Test
	public void create() {

		ExecutorService service = Executors.newFixedThreadPool(4);

		int count = 0;

		ThreadService thread1 = new ThreadService(count);
		ThreadService thread2 = new ThreadService(count);
		ThreadService thread3 = new ThreadService(count);
		ThreadService thread4 = new ThreadService(count);

		service.execute(thread1);
		service.execute(thread2);
		service.execute(thread3);
		service.execute(thread4);

		assertThat(service.isShutdown(), is(false));

		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		service.shutdown();

		assertThat(service.isShutdown(), is(true));

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertThat(thread1.stopped(), is(true));
		assertThat(thread2.stopped(), is(true));
		assertThat(thread3.stopped(), is(true));
		assertThat(thread4.stopped(), is(true));
	}

	private static class ThreadService implements Runnable {

		private int count = 0;
		private boolean stopped;

		public ThreadService(int inputCount) {
			ThreadExecutorTest.log.warn(String.format(
					"Initialized new ThreadService, starting work on count: %d", count));
			this.count = inputCount;
			this.stopped = false;
		}

		@Override
		public void run() {
			while (!stopped) {
				count++;

				ThreadExecutorTest.log.info(String.format("Increased Counter to: %d", count));

				if (count == 10) {
					stopped = true;
					break;
				}
			}
		}

		public boolean stopped() {
			return this.stopped;
		}
	}

}
