package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Check;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.CheckFailedException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Consumer;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Producer;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.SelectorPool;

public class IntegrationTestCase {

	public static final File outputFile = new File("outputfile.txt");
	protected static final int MAX = 100;

	@After
	public void tearDown() {
		if (outputFile.exists()) {
			outputFile.delete();
		}
	}

	@Test
	public void test() throws IOException, AssertionError {
		ExecutorService executor = Executors.newFixedThreadPool(2);

		final SelectorPool<Integer> stringPool = new SelectorPool<Integer>("") {
			private final int ProductionMax = IntegrationTestCase.MAX;
			private final AtomicInteger counter = new AtomicInteger(0);

			@Override
			protected Integer create() throws ReachedEndException {
				if (this.counter.get() >= this.ProductionMax) { throw new ReachedEndException(); }
				return this.counter.incrementAndGet();
			}

			@Override
			public int size() {
				return this.ProductionMax;
			}

			@Override
			public boolean validate(Integer inputObj) {
				return inputObj != null && inputObj <= this.ProductionMax;
			}

			@Override
			protected void releaseObj(Integer inputObj) {
				// nothing to do here, because the string gets released no
				// matter what
			}
		};

		final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

		executor.execute(new StringCreator(queue, stringPool));
		executor.execute(new StringConsumer(queue, new StringCheck()));

		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		assertContent(outputFile);

	}

	private void assertContent(final File inputOutputfile) throws AssertionError, IOException {
		assertThat(inputOutputfile.exists(), is(true));

		final BufferedReader reader = new BufferedReader(new FileReader(inputOutputfile));

		assertThat(reader, notNullValue());

		int count = 0;
		String line = null;
		while ((line = reader.readLine()) != null) {
			count++;
			assertThat(line, line, is(String.format("%s: nr %d", StringCreator.msg, count)));
		}

		assertThat(count, is(MAX));
	}

	public static class StringCreator extends Producer<Integer, String> {

		private static final Logger log = LoggerFactory.getLogger(StringCreator.class);

		public static final String msg = "The created message";

		public StringCreator(final BlockingQueue<String> inputQueue, final SelectorPool<Integer> inputPool) {
			super(inputQueue, inputPool);
		}

		@Override
		public String produce(final Integer inputNr) throws InterruptedException {
			return String.format("%s: nr %d", msg, inputNr);
		}

	}

	public static class StringConsumer extends Consumer<String> {

		private static final Logger log = LoggerFactory.getLogger(StringCreator.class);

		private static final AtomicInteger counter = new AtomicInteger(0);

		private final String instance_name;

		public StringConsumer(final BlockingQueue<String> inputQueue, final Check<String> inputCheck) {
			super(inputQueue, inputCheck, null);
			this.instance_name = String.format("%s_%d", "consumer", counter.incrementAndGet());
		}

		@Override
		public void consume(final String inputString) throws InterruptedException {

			try {
				FileWriter writer = new FileWriter(outputFile, true);
				writer.append(inputString);
				writer.append("\n");
				writer.flush();
				writer.close();
			} catch (IOException ex) {
				throw new InterruptedException();
			}

			log.info(String.format("[%s]: Consumed string successfully.", this.getIdentifier()));

		}

	}

	public static class StringCheck extends Check<String> {

		@Override
		public void validate(String inputString) throws CheckFailedException {
			if (inputString == null) { throw new CheckFailedException(); }
		}

	}

}
