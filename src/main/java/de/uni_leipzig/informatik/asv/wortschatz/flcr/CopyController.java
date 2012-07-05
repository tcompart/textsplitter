package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Check;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.CheckFailedException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Command;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Consumer;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Producer;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Task;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextfilePool;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.InstanceNamePatternFactory;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReserverUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.SelectorPool;

public class CopyController implements Runnable {

	public static final Pattern fileNamePattern = Textfile.inputFileNamePattern;

	private static final Logger log = LoggerFactory.getLogger(CopyController.class);

	// TODO re-think about this kind of stop, because the copy controller
	// listens to the executor, and this can be shutdown...
	private volatile boolean stop = true;

	private SelectorPool<Textfile> textfilePool;

	private ExecutorService executor;

	private MappingFactory mappingFactory;

	private BlockingQueue<Boolean> resultQueue;

	private final String instance_name;

	private final Configurator configuration;

	public CopyController() {
		this(Configurator.getConfiguration());
	}

	public CopyController(final Configurator configurator) {
		
		if (configurator == null) {
			throw new NullPointerException();
		}
		
		this.instance_name = InstanceNamePatternFactory.getInstanceName(getClass());
		this.configuration = configurator;
	}
	
	public void start(final Collection<File> inputFiles) {

		if (inputFiles == null) { throw new NullPointerException(); }

		final List<File> foundFilesWithPattern = new ArrayList<File>();

		loadFiles(inputFiles, foundFilesWithPattern, fileNamePattern);
		this.setTextilePool(new TextfilePool(foundFilesWithPattern));
		this.setExecutor(Executors.newFixedThreadPool(3));

		this.mappingFactory = this.getMappingFactory();
		this.resultQueue = new LinkedBlockingQueue<Boolean>();
		
		assert this.textfilePool != null;
		assert this.executor != null;
		assert this.mappingFactory != null;

		this.getExecutor().execute(this);
		
		log.info(String.format(
				"An instance of class '%s' started running: %s", CopyController.class.getSimpleName(),
				this.instance_name));
	}

	protected void setTextilePool(final SelectorPool<Textfile> inputTextfilePool) {
		if (inputTextfilePool == null) { throw new NullPointerException(); }
		if (inputTextfilePool.size() == 0 || inputTextfilePool.finished()) { throw new IllegalArgumentException(
				String.format(
						"You assigned an already empty instance of class '%s'. Please fill the pool with information.",
						SelectorPool.class.getName())); }
		this.textfilePool = inputTextfilePool;
	}

	public void setMappingFactory(MappingFactory inputMappingFactory) {
		if (inputMappingFactory == null) { throw new NullPointerException(); }
		this.mappingFactory = inputMappingFactory;
	}

	public MappingFactory getMappingFactory() {
		if (this.mappingFactory == null)
			this.mappingFactory = new MappingFactory(this.configuration);
		return this.mappingFactory;
	}
	
	protected void setExecutor(ExecutorService inputExecutor) {
		if (inputExecutor == null) { throw new NullPointerException(); }
		this.executor = inputExecutor;
	}

	@Override
	public void run() {

		if (this.getTextfilePool() == null || this.getExecutor() == null) { 
			throw new IllegalStateException(String.format("This instance of class '%s' reached an illegal state. It copied already a number of files, was stoped or finished it assigned job. Start this instance again, if you want to run the controller again.", CopyController.class.getName())); 
		}

		if (isRunning()) {
			log.info(String.format("This instance of class '%s' is already running: %s", CopyController.class.getName(), this.instance_name));
			return;
		}
		
		this.stop = false;
		try {
			assert this.isStoped() == false;
			assert this.isRunning() == true;

			final BlockingQueue<Set<Task>> queue = new SynchronousQueue<Set<Task>>();
			final SelectorPool<Textfile> pool = this.textfilePool;
			final MappingFactory localMappingFactory = this.getMappingFactory();

			final int numberOfCores = Runtime.getRuntime().availableProcessors();

			assert numberOfCores > 0;

			if (log.isDebugEnabled()) {
				log.debug(String.format("[%s]: number of cores: %d", this.instance_name, numberOfCores));
				log.debug(String.format("[%s]: number of entries in selector pool: %d", this.instance_name, pool.size()));
				log.debug(String.format("[%s]: number of entries in blocking queue: %d", this.instance_name, queue.size()));
			}
			
			// with the producer consumer principle, at least one			
			// producer/consumer should exist
			for (int i = 0; i < numberOfCores; i++) {
				// first create an producer
				log.debug(String.format("[%s]: adding new instance of class %s to the executor.", this.instance_name,TaskProducer.class.getSimpleName()));
				this.getExecutor().execute(new TaskProducer(queue, pool, localMappingFactory, this.resultQueue));
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					// ignore an interuption of sleep
				}
				// second create an consumer
				log.debug(String.format("[%s]: adding new instance of class %s to the executor.", this.instance_name,TaskConsumer.class.getSimpleName()));
				this.getExecutor().execute(new TaskConsumer(queue, new TaskCheck(), this.resultQueue));
			}
			/*
			 * after this thread has to exist further, but should check
			 * periodically
			 * if it should stop the whole process (executor) or not.
			 */
			while (!Thread.interrupted()) {

				if (log.isDebugEnabled()) {
					log.debug(String.format("Number of entries in pool: %d", pool.size()));
					log.debug(String.format("Number of entries in queue: %d", queue.size()));
					log.debug(String.format("Number of reserved files: %d", ReserverUtil.numberOfReservedFiles()));
				}

				if (this.isStoped()) {
					log.debug(String.format("Reached possible point of break for class '%s'. Stopping this thread now.", CopyController.class.getSimpleName()));
					break;
				}

				/*
				 * keep the thread or better, the executor running till the pool
				 * finished, or the the thread stopped unexpectedly by
				 * interrupting
				 * the process. That means however, the finally part has to be
				 * executed, not matter what!!!
				 */
				try {
					synchronized (this) {
						this.wait(2500);
					}
				} catch (InterruptedException ex) {
					// kindly ignore this exception, because it just does not
					// matter how long this thread will sleep. However, it
					// should find an end
				}

				if (pool.finished() && queue.isEmpty() && ReserverUtil.numberOfReservedFiles() == 0) {
					log.info("Stopping this thread, because the number of entries in the pool, the queue and number of reseved files are all 0.");
					break;
				}
			}

		} finally {
			this.unsetInstances();
			this.stop();
		}

		assert this.textfilePool == null;
		assert this.executor == null;

		assert this.isRunning() == false;
		assert this.isStoped() == true;

	}

	private SelectorPool<Textfile> getTextfilePool() {
		return this.textfilePool;
	}

	private ExecutorService getExecutor() {
		return this.executor;
	}

	private void unsetInstances() {
		this.textfilePool = null;
		this.executor.shutdown();
		this.executor = null;
	}

	public void stop() {
		if (!this.stop) {
			this.stop = !stop;
			log.info(String.format("Received order to stop this instance."));
		}
		assert this.stop == true;
	}

	public boolean isRunning() {
		return (!this.isStoped());
	}

	public boolean isStoped() {
		return this.stop;
	}

	public boolean isSuccessful() {
		// this means, the instance can be still running... and is still successful.
		return this.resultQueue != null && !this.resultQueue.isEmpty() && !this.resultQueue.contains(false);
	}

	public static void loadFiles(final Collection<File> inputFiles, final Collection<File> files,
			final Pattern inputFilenamepattern) {

		if (log.isDebugEnabled()) {
			log.debug(String.format("Number of assigned input files: %d", inputFiles.size()));
		}

		/*
		 * the file sourceLocationPattern should never be null
		 * there if the assigned file sourceLocationPattern
		 * use the already internal file sourceLocationPattern
		 */
		final Pattern filePattern;
		if (inputFilenamepattern != null) {
			filePattern = inputFilenamepattern;
		} else {
			filePattern = fileNamePattern;
		}

		final FilenameFilter fileNameFilter = new FilenameFilter() {
			private final Logger log = LoggerFactory.getLogger(FilenameFilter.class);

			@Override
			public boolean accept(File dir, String fileName) {
				String directoryName = dir.getName();
				this.log.debug(String.format("The following file name has to be checked: %s", fileName));
				Matcher matcher = filePattern.matcher(fileName);
				return ((directoryName.equals("Stopwort") || directoryName.equals("Unigramm") || directoryName
						.equals("Trigramm")) && (matcher.find()));
			}
		};

		for (File inputFile : inputFiles) {
			if (inputFile.isFile()) {
				Matcher matcher = filePattern.matcher(inputFile.getName());
				if (log.isDebugEnabled()) {
					log.debug(String.format("The following file name has to be checked: %s", inputFile.getName()));
				}
				if (matcher.find()) {
					files.add(inputFile);
				}
			} else if (inputFile.isDirectory()) {
				final File directory = inputFile;
				File[] array = directory.listFiles(fileNameFilter);

				if (array != null && array.length > 0) {
					for (File file : array) {
						files.add(file);
					}
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("Number of assigned output files: %d", files.size()));
		}
	}

	public static class TaskProducer extends Producer<Textfile, Set<Task>> {

		private static final Logger log = LoggerFactory.getLogger(TaskProducer.class);

		private final MappingFactory taskProducerMappingFactory;

		private final String instance_name;

		public TaskProducer(final BlockingQueue<Set<Task>> inputQueue, final SelectorPool<Textfile> inputPool,
				MappingFactory inputFactory, final BlockingQueue<Boolean> resultQueue) {
			super(inputQueue, inputPool, resultQueue);
			this.taskProducerMappingFactory = inputFactory;
			this.instance_name = InstanceNamePatternFactory.getInstanceName(getClass());
			log.info(String.format(
					"Starting new instance of class '%s': %s", TaskProducer.class.getSimpleName(), this.instance_name));
		}

		@Override
		public Set<Task> produce(final Textfile inputTextfile) throws InterruptedException {

			log.warn(String.format("Number of entries in queue: %d.", this.getQueue().size()));

			final Set<Task> result = new LinkedHashSet<Task>();
			try {
				for (Source source = inputTextfile.getNext(); source != null; source = inputTextfile.getNext()) {
					final File outputFile = this.taskProducerMappingFactory.getSourceDomainMapping(inputTextfile, source);
					result.add(new Task(new CopyCommand(source, outputFile)));
				}
			} catch (ReachedEndException ex) {
				log.info(String.format(
						"[%s]: reached end of production cycle of assiged %s. Produced number of %s: %d",
						this.instance_name, Textfile.class.getSimpleName(), Task.class.getSimpleName(), result.size()));
			}
			
			inputTextfile.release();
			
			return result;
		}
	}

	public static class TaskConsumer extends Consumer<Set<Task>> {

		private static final Logger log = LoggerFactory.getLogger(TaskConsumer.class);

		private final String instance_name;

		public TaskConsumer(final BlockingQueue<Set<Task>> inputQueue, final BlockingQueue<Boolean> inputResultQueue) {
			this(inputQueue, null, inputResultQueue);
		}

		public TaskConsumer(final BlockingQueue<Set<Task>> inputQueue, final Check<Set<Task>> inputCheck, final BlockingQueue<Boolean> inputResultQueue) {
			super(inputQueue, inputCheck, inputResultQueue);
			this.instance_name = InstanceNamePatternFactory.getInstanceName(getClass());
			log.info("Starting new instance of class {}: '{}'", TaskConsumer.class.getSimpleName(), this.instance_name);
		}

		@Override
		public void consume(final Set<Task> inputSet) throws InterruptedException {
			for (Task task : inputSet) {
				if (log.isInfoEnabled()) {
					log.info("[{}]: Consuming next task '{}'", this.instance_name, task.getUniqueIdentifier());
				}
				task.doTask();
			}
		}
	}

	public static class TaskCheck extends Check<Set<Task>> {

		private static Logger log = LoggerFactory.getLogger(TaskCheck.class);
		
		public static void validate(final Task inputTask) throws CheckFailedException {
			
			if (log.isInfoEnabled()) {
				if (inputTask != null && !inputTask.finished()) {
					log.info("Assigned task '{}' not finished yet.", inputTask.getUniqueIdentifier());
				} else if (inputTask != null && inputTask.successful()) {
					log.info("Assigned task '{}' finished successfully.", inputTask.getUniqueIdentifier());
				}
			}
			
			if (inputTask == null || !inputTask.finished() || !inputTask.successful()) { throw new CheckFailedException(); }
		}

		@Override
		public void validate(final Set<Task> inputSet) throws CheckFailedException {
			if (inputSet == null) { throw new NullPointerException(); }
			for (Task task : inputSet) {
				validate(task);
			}
		}

	}

	public static class CopyCommand implements Command {

		private final static Logger log = LoggerFactory.getLogger(CopyCommand.class);

		private final Source source;
		private final File file;

		public CopyCommand(final Source inputSource, final File outputFile) {
			this.source = inputSource;
			this.file = outputFile;
		}

		@Override
		public void execute() throws CommandExecutionException {
			File temporaryFile = null;
			while (temporaryFile == null) {
				try {
					temporaryFile = ReserverUtil.reserve(this.file).getValue();
				} catch (InterruptedException ex) {
					if (log.isDebugEnabled()) {
						log.debug("{}: reservation of output file '{}' was interrupted. Trying again.", 1, (temporaryFile != null ? temporaryFile.getName() : "not yet known!"));
					}
				}
			}
			
			final File outputfile = temporaryFile;
			
			if (log.isDebugEnabled()) {
				log.debug("{}: reserved output file '{}'", 1, outputfile.getName());
			}
			
			if (CopyCommand.createOutputDirectories(outputfile)) {
				if (log.isDebugEnabled()) {
				log.debug("{}: created output directories '{}'", 2, outputfile.getParentFile().getAbsolutePath());
				}
			} else {
				log.warn("%d: creating of output directories '%s' failed.", 2, outputfile.getParentFile().getAbsolutePath());
			}

			
			if (log.isDebugEnabled()) {
				log.debug("{}: executing copy command.", 3);
			}
			
			try {
				long preCopyLength = outputfile.length();
				if (CopyCommand.copy(this.source, outputfile)) {
					final long afterCopyLength = outputfile.length();
					final Long byteDiffLength = afterCopyLength-preCopyLength;
					log.debug("{}: finished copying. Copied {} bytes to file '{}'", new Object[]{ 4, byteDiffLength, outputfile.getAbsolutePath()});
				} else {
					throw new CommandExecutionException(String.format("%d: COPYING FAILED! FILE '%s' MAY BE CORRUPTED NOW.", 4, outputfile.getAbsolutePath()));
				}
			} catch (IOException ex) {
				// the only solution would be: deletion of outputfile, repeating
				// of all writing (every source, which was already written +
				// this.source) other solution: keep an backup, if this fails, re-use the
				// backup, and re-try writing source
				log.error("While executing {} {} an exception occurred. Please check the stack trace for more details.",new Object[]{Command.class.getSimpleName(), CopyCommand.class.getSimpleName()}, ex);
				throw new CommandExecutionException(String.format("This means a serious error. Some content was probably already written. Some was not. Corrupted file: '%s'", outputfile.getName()), ex);
			} finally {
				ReserverUtil.release(this.file);
				
				if (log.isDebugEnabled())
					log.debug(String.format("%d: released output file '%s'", 5, outputfile.getName()));
				
			}
		}

		public static boolean createOutputDirectories(final File inputFile) {
			if (inputFile == null) { throw new NullPointerException(); }
			if (!inputFile.exists() && !inputFile.getParentFile().exists()) {
				inputFile.getParentFile().mkdirs();
			}
			return inputFile.getParentFile().exists() && inputFile.getParentFile().isDirectory();
		}

		public static boolean copy(final Source inputSource, final File inputOutputFile) throws IOException {

			boolean localSuccessful = false;

			/*
			 * initialize the disposable parameters:
			 * Reader.content -> Writer.content till the line number was reached
			 * of
			 * the current source
			 */
			StringBuffer stringBuffer = null;
			Writer writer = null;
			try {
				stringBuffer = inputSource.getContent();
				writer = new FileWriter(inputOutputFile, true);
				writer.append(stringBuffer);
				// both, flush and successful should be done at the same time
				writer.flush();
				localSuccessful = true;
			} catch (FileNotFoundException ex) {
				localSuccessful = false;
				throw new IOException("An inner thread exception, meaning a non existing input file. That error would be the worst case/bug, because the input file has to be removed between the first check and this check.",ex);
			} catch (IOException ex) {
				localSuccessful = false;
				throw new IOException("Serious exception type while reading/writing.", ex);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException ex) {
						// TODO solve the head-ache which is created with this
						// kind
						// of exception

						// ignore.... but with head-ache
					}
				}
			}
			return localSuccessful;
		}

	}
}
