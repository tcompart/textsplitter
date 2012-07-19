package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import de.compart.gui.cli.Parameter;
import de.compart.gui.cli.ParameterFactory;
import de.compart.gui.cli.ParameterFactory.ParameterConfiguration;
import de.compart.gui.cli.ParameterResult;
import de.uni_leipzig.asv.clarin.common.tuple.Maybe;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;

public class ViewController implements Callable<Boolean> {

	public static final Parameter<?> HELP = ParameterFactory.createParameter(
			'h', "help", ParameterConfiguration.NO_ARGUMENTS,
			ParameterConfiguration.OPTIONAL);
	public static final Parameter<?> VERBOSE = ParameterFactory
			.createParameter('v', "verbose",
					ParameterConfiguration.NO_ARGUMENTS,
					ParameterConfiguration.OPTIONAL);
	public static final Parameter<File> OUTPUT = ParameterFactory
			.createParameter('o', "output",
					ParameterConfiguration.ONE_ARGUMENT, File.class,
					ParameterConfiguration.OPTIONAL);
	public static final Parameter<File> INPUT = ParameterFactory
			.createParameter('i', "input", ParameterConfiguration.ONE_ARGUMENT,
					File.class, ParameterConfiguration.REQUIRED);
	public static final Parameter<File> PROPERTY = ParameterFactory
			.createParameter('p', "properties",
					ParameterConfiguration.ONE_ARGUMENT, File.class,
					ParameterConfiguration.OPTIONAL);
	public static final Parameter<?> NO_THREADS = ParameterFactory
			.createParameter(ParameterFactory.NO_SHORT_OPTION, "no-threads",
					ParameterConfiguration.NO_ARGUMENTS,
					ParameterConfiguration.OPTIONAL);
	public static final Parameter<?> DEBUG = ParameterFactory.createParameter(
			'd', "debug", ParameterConfiguration.NO_ARGUMENTS,
			ParameterConfiguration.OPTIONAL);
	public static final Parameter<?> STDOUT = ParameterFactory.createParameter(
			's', "stdout", ParameterConfiguration.NO_ARGUMENTS,
			ParameterConfiguration.OPTIONAL);
	public static final Parameter<Integer> NR_THREADS = ParameterFactory
			.createParameter('n', "numberOfThreads",
					ParameterConfiguration.ONE_ARGUMENT, Integer.class,
					ParameterConfiguration.OPTIONAL);

	private static final Logger log = LoggerFactory
			.getLogger(ViewController.class);

	private static final AtomicInteger instance_counter = new AtomicInteger(0);
	private final Configurator configuration;
	private final int instance_number;
	private final File flcrInputDirectory;
	private final boolean useStdout;
	private final int numberOfThreads;

	public ViewController(final Properties properties, final File inputFile,
			final int numberOfThreads, boolean stdout)
			throws FileNotFoundException {
		this(new Configurator(properties), inputFile, numberOfThreads, stdout);
	}

	public ViewController(final Configurator configurator,
			final File inputDirectory, final int inputNumberOfThreads,
			boolean stdout) throws FileNotFoundException {
		if (configurator == null) {
			throw new IllegalArgumentException(String.format(
					"Assigned parameter instance of class %s points to null.",
					Configurator.class.getSimpleName()));
		}

		// the next statement would provoke a null pointer exception, if
		// inputFile would be null (therefore no NPE for inputfile)
		if (!inputDirectory.exists() || !inputDirectory.canRead()) {
			throw new FileNotFoundException(
					String.format(
							"File '%s' could not be found. Furthermore it has to be redable!",
							inputDirectory.getAbsolutePath()));
		}

		this.configuration = configurator;
		this.instance_number = instance_counter.incrementAndGet();
		this.flcrInputDirectory = inputDirectory;
		this.numberOfThreads = inputNumberOfThreads;
		this.useStdout = stdout;
		log.info("Initiliazed {} instance '{}'.",
				ViewController.class.getSimpleName(), this);
	}

	@Override
	public String toString() {
		return String.format("%s_%d {Configuration='%s', File='%s'}",
				ViewController.class.getSimpleName(), this.instance_number,
				this.configuration, this.flcrInputDirectory.getAbsolutePath());
	}

	@Override
	public Boolean call() throws Exception {

		if (useStdout)
			System.out.println("Starting CopyManager");

		final CopyManager copy;
		if (numberOfThreads > 0)
			copy = new ComplexCopyManager(this.flcrInputDirectory,
					this.configuration, numberOfThreads);
		else
			copy = new SimpleCopyManager(this.flcrInputDirectory,
					this.configuration);

		copy.setOutputStream(System.out);
		copy.start();

		// because of threads....
		if (copy instanceof ComplexCopyManager) {
			((ComplexCopyManager) copy).awaitTermination();
		}

		final boolean result = copy.isStoped() && copy.isSuccessful();

		if (useStdout)
			System.out
					.println("Stoping CopyManager:  => stoped & succesful == "
							+ result);

		return result;
	}

	public static void main(String... args) throws IOException,
			InterruptedException, ExecutionException {

		Collection<Parameter<?>> allParameters = Collections
				.unmodifiableList(new ArrayList<Parameter<?>>() {

					private static final long serialVersionUID = 1L;
					{
						add(HELP);
						add(VERBOSE);
						add(OUTPUT);
						add(INPUT);
						add(PROPERTY);
						add(NO_THREADS);
						add(DEBUG);
						add(STDOUT);
						add(NR_THREADS);
					}
				});
		// parse parameters with defined parameters
		ParameterResult result = ParameterFactory.parse(args, allParameters);

		/*
		 * 1. CHECK FOR POSSIBLE HELP
		 */

		assert System.out instanceof OutputStream;
		assert System.err instanceof OutputStream;

		if (result.hasParameter(VERBOSE)) {
			org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
		} else if (result.hasParameter(DEBUG)) {
			org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
		} else {
			org.apache.log4j.Logger.getRootLogger().setLevel(Level.WARN);
		}

		if (result.hasParameter(HELP)) {
			ParameterFactory.printHelp(System.out, System.err, result,
					allParameters);
			// break the loop
			return;
		}

		/*
		 * 2. CHECK INPUT DIRECTORY
		 */
		final Maybe<File> maybeInput = result.getValue(INPUT);
		if (maybeInput.isNothing()) {
			throw new IllegalArgumentException(String.format(
					"Requiring the input directory assigned by parameter '%s'",
					INPUT));
		} else if (!maybeInput.getValue().exists()) {
			ParameterFactory.printHelp(System.out, System.err, result,
					allParameters);
			throw new FileNotFoundException(
					"Requiring input directory. Please assign an existing directory file path as a source directory.");
		}
		final File inputFile = maybeInput.getValue();

		/*
		 * 3. CREATE PROPERTIES (OVERALL APPLICATION CONFIGURATION)
		 */
		final Properties properties = new Properties();

		/*
		 * 3.1 INTERNAL CLASSPATH PROPERTIES
		 */
		final String defaultPropertyName = "default.properties";
		final Resource classPathResource = new ClassPathResource(
				defaultPropertyName);
		if (classPathResource.exists()) {
			log.info("Loading properties of property file '{}'.",
					defaultPropertyName);
			properties.load(classPathResource.getInputStream());
		} else {
			log.warn("Default properties '{}' of application did not exist.",
					defaultPropertyName);
		}

		/*
		 * 3.2 EXTERNAL FILE SYSTEM PROPERTIES (OVERWRITTING DEFAULT PROPERTIES)
		 */
		// file system resource will therefore overwrite classpath resource
		// properties
		final String applicationPropertyName = "application.properties";
		final Resource fileSystemResource = new FileSystemResource(
				applicationPropertyName);
		if (fileSystemResource.exists()) {
			log.info("Loading properties of property file '{}'.",
					applicationPropertyName);
			properties.load(fileSystemResource.getInputStream());
		} else {
			log.warn(
					"Application properties '{}' in the current directory could not be found.",
					applicationPropertyName);
		}

		/*
		 * 3.3 ASSIGING A THIRD INSTANCE OF POSSIBLE PROPERTY FILE
		 */
		final Maybe<File> maybeProperties = result.getValue(PROPERTY);
		if (maybeProperties.isJust() && maybeProperties.getValue().exists()) {
			// and finally PROPERTY declaration will overwrite CLASSPATH and
			// FILESYSTEM DEFAULT
			properties.load(new FileInputStream(maybeProperties.getValue()));
		} else {
			log.warn(
					"Assuming the default properties previously defined or directly by class '{}' if not defined",
					Configurator.class.getName());
		}

		/*
		 * 4. CHANGE THE OUTPUT DIRECTORY BY CALLING THE PARAMETER DIRECTLY
		 */
		final Maybe<File> maybeOutput = result.getValue(OUTPUT);
		if (maybeOutput.isJust()) {
			final File outputDirectory = maybeOutput.getValue();
			if (outputDirectory.mkdirs()
					|| (outputDirectory.exists() && outputDirectory
							.isDirectory())) {
				properties.setProperty(Configurator.PROPERTY_BASE_OUTPUT,
						outputDirectory.getAbsolutePath());
				log.info("Setting output directory to '{}'",
						outputDirectory.getAbsolutePath());
			} else
				throw new IllegalArgumentException(
						"The assigned output directory");
		} else {
			log.warn(
					"Assuming output directory to be the default output directory: '{}'",
					Configurator.DEFAULT_BASE_OUTPUT_DIRECTORY);
		}

		int numberOfThreads;
		if (result.hasParameter(NO_THREADS)) {
			numberOfThreads = 0;
			log.warn("Deactivating thread-support. This may decrese the through-put speed.");
		} else {
			// this means that 2 threads run parallel as one set
			numberOfThreads = 1;
			// a number higher than 1 would mean, the number of parallel sets
			// running: 2 means therefore 2 reader with 2 writer
			if (result.hasParameter(NR_THREADS)) {
				Maybe<Integer> maybeNumberOfThreadPairs = result.getValue(NR_THREADS);
				if (maybeNumberOfThreadPairs.isJust())
					numberOfThreads = maybeNumberOfThreadPairs.getValue();
				else
					throw new IllegalArgumentException(String.format("You assigned the parameter {} with a not understandable value: {}", NR_THREADS, result.getValue(NR_THREADS, String.class)));
			}
		}

		final boolean useStdout;
		if (result.hasParameter(STDOUT)) {
			useStdout = true;
			log.info("Activating status messages to System.out.");
		} else {
			useStdout = false;
		}

		if (result.hasParameter(DEBUG)) {
			properties.store(System.out, "the loaded properties");
			return;
		}

		final ExecutorService ecs = Executors.newSingleThreadExecutor();
		final Future<Boolean> future = ecs.submit(new ViewController(
				properties, inputFile, numberOfThreads, useStdout));

		future.get();

		System.exit(0);
	}

}
