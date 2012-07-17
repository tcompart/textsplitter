package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.asv.clarin.common.tuple.Maybe;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Command;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReserverUtil;

public class CopyCommand implements Command {

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
			final Maybe<File> maybeReservedFile = ReserverUtil.reserve(this.file);
			if (maybeReservedFile.isNothing()) {
				try {
					synchronized (this) {
						this.wait(1000);
					}
				} catch (InterruptedException ex) {
					log.warn(
							"{}: reservation of output file '{}' was interrupted. Trying again.",
							1, (temporaryFile != null ? temporaryFile.getName()
									: "not yet known!"));
				}
				continue;
			}
			temporaryFile = maybeReservedFile.getValue();
		}

		final File outputfile = temporaryFile;

		final String instance_name = outputfile.getName();

		log.debug("[{}; {}]: reserved output file '{}'", new Object[] {
				instance_name, 1, outputfile.getName() });

		if (CopyCommand.createOutputDirectories(outputfile)) {
			log.debug("[{}; {}]: created output directories '{}'",
					new Object[] { instance_name, 2,
							outputfile.getParentFile().getAbsolutePath() });
		} else {
			log.warn(
					"[{}; {}]: creating of output directories '{}' failed.",
					new Object[] { instance_name, 2,
							outputfile.getParentFile().getAbsolutePath() });
		}

		log.debug("[{}; {}]: executing copy command.", instance_name, 3);

		try {
			long preCopyLength = outputfile.length();
			if (CopyCommand.copy(this.source, outputfile)) {
				final long afterCopyLength = outputfile.length();
				final Long byteDiffLength = afterCopyLength - preCopyLength;
				log.debug(
						"[{}; {}]: finished copying. Copied {} bytes to file '{}'",
						new Object[] { instance_name, 4, byteDiffLength,
								outputfile.getAbsolutePath() });
			} else {
				throw new CommandExecutionException(
						String.format(
								"%d: COPYING FAILED! FILE '%s' MAY BE CORRUPTED NOW.",
								4, outputfile.getAbsolutePath()));
			}
		} catch (IOException ex) {
			// the only solution would be: deletion of outputfile, repeating
			// of all writing (every source, which was already written +
			// this.source) other solution: keep an backup, if this fails,
			// re-use the
			// backup, and re-try writing source
			log.error(
					"While executing {} {} for output file '{}' an exception occurred. Please check the stack trace for more details.",
					new Object[] { Command.class.getSimpleName(),
							CopyCommand.class.getSimpleName(),
							outputfile.getAbsolutePath() }, ex);
			throw new CommandExecutionException(
					String.format(
							"This means a serious error. Some content was probably already written. Some was not. Corrupted file: '%s'",
							outputfile.getName()), ex);
		} finally {
			
			log.info("Executing finally tree.");
			
			if (ReserverUtil.release(this.file)) {
				log.info("[{}; {}]: released output file '{}'", new Object[] {
						instance_name, 5, outputfile.getName() });
			} else {
				log.warn("[{}; {}]: output file '{}' was not reserved?!", new Object[] {
						instance_name, 5, outputfile.getName() });
			}


		}
	}

	public static boolean createOutputDirectories(final File inputFile) {
		if (inputFile == null) {
			throw new NullPointerException();
		}
		if (!inputFile.exists() && !inputFile.getParentFile().exists()) {
			inputFile.getParentFile().mkdirs();
		}
		return inputFile.getParentFile().exists()
				&& inputFile.getParentFile().isDirectory();
	}

	public static boolean copy(final Source inputSource,
			final File inputOutputFile) throws IOException {

		boolean localSuccessful = false;

		Writer writer = null;
		try {
			final StringBuffer stringBuffer = inputSource.getContent();
			writer = new FileWriter(inputOutputFile, true);
			try {
				writer.append(stringBuffer);
			} catch (OutOfMemoryError ex) {
				final String errorMsg = String.format("%s: source '%s' (line nr: '%d') for outputfile '%s' ", ex.getMessage(), inputSource, inputSource.getLineNumber(), inputOutputFile);
				throw new OutOfMemoryError(errorMsg);
			}
			
			// both, flush and successful should be done at the same time
			writer.flush();
			localSuccessful = true;
		} catch (FileNotFoundException ex) {
			localSuccessful = false;
			throw new IOException(
					"An inner thread exception, meaning a non existing input file. That error would be the worst case/bug, because the input file has to be removed between the first check and this check.",
					ex);
		} catch (IOException ex) {
			localSuccessful = false;
			throw new IOException(
					"Serious exception type while reading/writing.", ex);
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