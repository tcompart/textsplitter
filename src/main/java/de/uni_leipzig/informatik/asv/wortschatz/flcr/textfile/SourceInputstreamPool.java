package de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr._development.Development;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.SelectorPool;

public class SourceInputstreamPool extends SelectorPool<Source> {
	
	public static final String SOURCE_START = "<source><location>";

	private static final Logger log = LoggerFactory.getLogger(SourceInputstreamPool.class);

	private static final int CAPACITY = 3;
	
	private final AtomicInteger sourceCounter = new AtomicInteger(0);

	private final BlockingQueue<Source> queue = new LinkedBlockingQueue<Source>(CAPACITY);

	private final ExecutorService service;

	private final SourceFinder sourceFinder;

	public SourceInputstreamPool(final File inputFile) throws IOException {
		super(SourceInputstreamPool.class.getSimpleName());

		if (!inputFile.exists()) {
			throw new FileNotFoundException(String.format("File '%s' could not be found.", inputFile.getAbsolutePath()));
		}
		
		this.sourceFinder = new SourceFinder(new BufferedReader(new FileReader(inputFile)), this.queue, this.sourceCounter);
		
		this.service = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());		
		this.service.execute(sourceFinder);
				
	}

	@Override
	protected synchronized Source create() throws ReachedEndException, InterruptedException {

		if (this.queue.size() <= 1) {
			Thread.sleep(10);
		}
		
		assert this.queue.size() <= CAPACITY;
		
		Source source = this.queue.poll(1, TimeUnit.SECONDS);

		if (source == null) { throw new ReachedEndException(); }

		return source;
	}

	@Override
	public int size() {
		return this.sourceCounter.intValue();
	}

	@Override
	public boolean validate(final Source inputObj) {
		boolean result = (inputObj != null && inputObj.getContent().length() > 0);
		if (Development.assertionsEvaluated) {
			assert result : String.format("Invalid source: '%s'", inputObj.toString());
		}
		return result;
	}

	@Override
	protected void releaseObj(final Source inputObj) {

		// probably false... depending on the queue, because the object was taken from, and should not exist in the queue anymore
		this.queue.remove(inputObj);
	}

	public static class SourceFinder implements Runnable {

		private static final Logger log = LoggerFactory.getLogger(SourceFinder.class);
		
		private static final boolean DEBUG_ENABLED = log.isDebugEnabled();
		private static final boolean INFO_ENABLED = log.isDebugEnabled();
		
		private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);
		
		private final BufferedReader reader;
		private final BlockingQueue<Source> queue;
		private final AtomicInteger counter;

		private final String instance_name;

		private boolean finished;

		public SourceFinder(final BufferedReader inputReader, BlockingQueue<Source> inputQueue,
				AtomicInteger inputSourceCounter) {
			if (inputReader == null || inputQueue == null || inputSourceCounter == null) { throw new NullPointerException(); }
			
			this.instance_name = String.format("%s_%d", SourceFinder.class.getSimpleName(), INSTANCE_COUNTER.incrementAndGet());
			
			this.finished = false;
			this.reader = inputReader;
			this.queue = inputQueue;
			this.counter = inputSourceCounter;
			
			if (INFO_ENABLED) {
				log.info(String.format("%s: created new thread.", instance_name));
			}
		}

		public boolean finished() {
			return this.finished;
		}

		@Override
		public void run() {
			
			if (DEBUG_ENABLED) {
				log.debug(String.format("%s: Starting run", instance_name));
			}
			
			LineNumberReader localReader;
			try {
				localReader = new LineNumberReader(this.getReader());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			AtomicInteger localCounter = new AtomicInteger(0);
			Source source = null;
			try {
				StringBuffer stringBuffer = null;
				String line = null;
				while ((line = localReader.readLine()) != null) {
					if (line.startsWith(SOURCE_START)) {
						
						if (DEBUG_ENABLED)
							log.debug(String.format("%s: found new source '%s'", instance_name, line));
						
						if (source != null) { // previous found source before new source overwrites variable
							this.publish(this.queue, source);
						}
						
						stringBuffer = new StringBuffer();
						source = new Source(line, stringBuffer);
						source.setLineNumber(localReader.getLineNumber());
					}
					
					if (Development.assertionsEvaluated) {
						assert source != null;
						assert stringBuffer != null;
					}
					
					stringBuffer.append(line);
					stringBuffer.append("\n");
				}
				
				if (source != null) { // last found source before EOF
					this.publish(this.queue, source);
				}
			} catch (IOException ex) {
				throw new RuntimeException(String.format(
						"Critical runtime exception. Unable to read via %s '%s'",
						LineNumberReader.class.getSimpleName(), localReader.toString()), ex);
			} catch (OutOfMemoryError ex) {
				throw new OutOfMemoryError(String.format("%s: after a number of collected sources '%d' (source '%s' at line '%d')", ex.getMessage(), localCounter.get(), source, source.getLineNumber()));
			} finally {
				this.finished = true;
				try {
					if (localReader != null) {
						try {
							this.releaseReader(localReader);
						} catch (InterruptedException e) {
							// can be ignored, because the reader gets released!
						}
						// and closed here
						localReader.close();
					}
				} catch (IOException ex) {
					// ignore...
				}
			}
		}

		private volatile boolean inUsage = false;
		
		public synchronized BufferedReader getReader() throws InterruptedException {
			while (inUsage) {
				synchronized (this) {
					this.wait();
				}
			}
			inUsage = true;
			return this.reader;
		}

		public synchronized void releaseReader(final BufferedReader inputReader) throws InterruptedException {
			if (inUsage && this.reader == inputReader) {
				inUsage = false;
			}
		}
		
		protected synchronized void publish(final BlockingQueue<Source> queue, final Source inputSource) {
			try {
				if (DEBUG_ENABLED)
					log.debug(String.format("%s: publishing previous found source '%s' with a filled string buffer cache ('%d' signs).", instance_name, inputSource.toString(), inputSource.getContent().length()));
				queue.put(inputSource);
				
				assert queue.size() <= CAPACITY;
				
			} catch (InterruptedException e) {
				final String errorMsg = String.format("Publishing was interrupted while offering source '%s' to queue.", inputSource.toString());
				log.error(errorMsg);
				throw new RuntimeException(errorMsg);
			}
			final int oldValue = this.counter.getAndIncrement();

			if (Development.assertionsEvaluated) {
				// assert !this.queue.isEmpty();
				// assert this.queue.contains(inputSource);
				assert oldValue == this.counter.get() - 1; // asserts that only one thread is running or that the publishing happens in a synchronized way

			}
		}

	}
}