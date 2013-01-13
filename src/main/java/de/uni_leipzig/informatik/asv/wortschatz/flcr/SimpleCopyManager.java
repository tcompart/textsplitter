package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Task;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;

public class SimpleCopyManager implements CopyManager {

	private static final Logger log = LoggerFactory.getLogger(SimpleCopyManager.class);
	private static final AtomicInteger instanceCount = new AtomicInteger(0);
	
	private final BlockingQueue<File> queue;
	private final MappingFactory mappingFactory;

	private final String instanceName;

	public SimpleCopyManager(final File inputDirectory, final Configurator inputConfigurator) throws FileNotFoundException {
		if (inputDirectory == null) { throw new NullPointerException(); }
		if (inputConfigurator == null) { throw new NullPointerException(); }
		if (!inputDirectory.exists()) { throw new FileNotFoundException(String.format("File '%s' does not exist.", inputDirectory.getAbsolutePath())); }
		if (!inputDirectory.canRead()) { throw new IllegalArgumentException(String.format("The assigned input '%s' is not readable!", inputDirectory.getAbsolutePath())); }
		this.mappingFactory = new MappingFactory(inputConfigurator);
		final Collection<File> files = IOUtil.getFiles(inputDirectory, true);
		
		assert files.isEmpty() == false;
		
		this.queue = new ArrayBlockingQueue<File>(files.size(), true, files);
		this.instanceName = String.format("%s_%d", SimpleCopyManager.class.getSimpleName().toLowerCase(), instanceCount.incrementAndGet());
	}
	
	private volatile boolean stop = true;
	private volatile boolean success = true;
	private PrintStream out;

	public BlockingQueue<File> getFileQueue() {
		return this.queue;
	}

	@Override
	public void start() {
		
		if (isRunning()) {
			log.error("An instance of class '{}' is already running, and therefore cannot be started again.", SimpleCopyManager.class.getName());
			return;
		}
		success = false;
		stop = false;
		
		File file;
		
		log.debug("Initializing {} by calling {} '{}'", new Object[]{MappingFactory.class.getSimpleName(), Configurator.class.getSimpleName(), this.getConfigurator().toString()});
		
		if (this.hasOutputStream()) {
			this.writeOutput(out, "Starting to query file queue. Number of entries: "+this.getFileQueue().size());
		}
		
		while (!isStoped()) {
			file = this.getFileQueue().poll();
			try {
				long textFileTime = System.nanoTime();
				final TextFile textFile = new TextFile(file);
				System.out.println(String.format("Required: '%d' ns for creating textFile", System.nanoTime()-textFileTime));
				Source source;
				
				if (this.hasOutputStream()) {
					this.writeOutput(out, "Took textFile '"+file.getAbsolutePath()+"'");
				}


				while ((source = textFile.getNext()) != null) {
					log.info("Taking next source of file '{}'", file.getName());
					
					if (this.hasOutputStream()) {
						this.writeOutput(out, "Working on textFile '"+file.getAbsolutePath()+"' with source '"+source.toString()+"'");
					}

					long time = System.nanoTime();
					Task task = new Task(new CopyCommand(source, mappingFactory.getSourceDomainMapping( textFile, source)));
					log.info("Creating next task '{}'", task.getUniqueIdentifier());
					task.doTask();
					log.info("Finished task '{}", task.getUniqueIdentifier());
					System.out.println(String.format("Required: %d ns", System.nanoTime()-time));
				}
			} catch (ReachedEndException ex) {
				log.info("Finished TextFile instance of file '{}'. Proceeding with next file if present.", file.getName());
			} catch (IOException ex) {
				log.error("TextFile instance could not be loaded because of file '{}'", file.getName());
			}
		}
		
		if (queue.isEmpty()) {
			log.info("Finished all files. No more files present in queue. Stopping here therefore.");
			this.success = true;
		}
		this.stop();
	}

	@Override
	public void stop() {
		if (!stop) {
			log.info("Stopping this copy manager instance.");
			stop = !stop;
		}
	}

	@Override
	public boolean isRunning() {
		return !this.isStoped();
	}

	@Override
	public boolean isStoped() {
		return this.stop;
	}

	@Override
	public boolean isSuccessful() {
		return this.success;
	}

	@Override
	public String getInstanceName() {
		return this.instanceName;
	}

	@Override
	public Configurator getConfigurator() {
		return this.getMappingFactory().getConfigurator();
	}

	@Override
	public MappingFactory getMappingFactory() {
		return this.mappingFactory;
	}

	@Override
	public boolean hasModule(Module<?> module) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addModule(Module<?> module) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeModule(Module<?> module) {
		// TODO Auto-generated method stub
		
	}
	
	public void writeOutput(final PrintStream out, final String msg) {
		out.println(msg);
	}

	public boolean hasOutputStream() {
		return this.out != null;
	}
	
	@Override
	public void setOutputStream(PrintStream inputOut) {
		if (inputOut == null) { throw new NullPointerException(); }
		this.out = inputOut;
	}

}
