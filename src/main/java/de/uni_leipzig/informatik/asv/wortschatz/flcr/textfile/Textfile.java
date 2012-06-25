/**
 * 
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.asv.clarin.common.tuple.Maybe;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.SelectorPool;

/**
 * @author <a href="mail:compart@informatik.uni-leipzig.de">Torsten Compart</a>
 * 
 */
/**
 * @author <a href="mail:compart@informatik.uni-leipzig.de">Torsten Compart</a>
 * 
 */
public class Textfile {

	/**
	 * This value should not change. This value is meant to parse a returned
	 * value for a default value, which
	 * would make the requested year invalid. This means, this is the
	 * initialized value, and therefore the default year, which
	 * is regarded as useless, and the real year is maybe
	 */
	public static final Integer DEFAULT_YEAR = 0;

	public static final Pattern outputFileNamePattern = Pattern
			.compile("([a-z]{3}((-|_)[a-z]+)*)_web(fl|cr)_(\\d{4})(_\\d{4})?\\.txt");
	
	public static final Pattern inputFileNamePattern = Pattern
			.compile("(FL|CR)(_(\\d{4}))?_([a-z]{3}((-|_)[a-z]+)*)\\d{4}\\.txt");
	
	private static final Logger log = LoggerFactory.getLogger(Textfile.class);

	private final String textfile_name;

	private File inputFile;
	private TextfileType textFileType;
	private SelectorPool<Source> sourcePool;

	private SourceNumberFinder sourceNumberFinder;

	public Textfile(final File file) throws IOException {
		this.inputFile = file;
		this.textFileType = TextfileType.parse(file.getName());
		this.sourcePool = new SourceInputstreamPool(file);

		this.textfile_name = file.getName();

		sourceNumberFinder = new SourceNumberFinder(file);
		new ExecutorCompletionService<Integer>(Executors.newSingleThreadExecutor()).submit(sourceNumberFinder);
		
		log.info(String.format(
				"[%s]: newly initialized (input file: %s, textfile type: %s)", this.textfile_name,
				this.getTextfileName(), this.getOutputType().toString()));
	}

	public String getTextfileName() {
		return this.textfile_name;
	}

	private Integer year = null;

	private boolean errorOccurred = false;
	
	public int getYear() {
		if (!errorOccurred && this.year == null) {
			final Maybe<Integer> maybeInputYear = searchYearInFileName(inputFileNamePattern,this.getTextfileName(), 3);
			final Maybe<Integer> maybeOutputYear = searchYearInFileName(outputFileNamePattern,this.getTextfileName(), 4);
			
			if (maybeInputYear.isJust()) {
				this.year = maybeInputYear.getValue();
			} else if (maybeOutputYear.isJust()) {
				this.year = maybeOutputYear.getValue();
			} else {
				this.year = null;
				this.errorOccurred = true;
			}
		}
		if (this.year == null) { return DEFAULT_YEAR; }
		return this.year;
	}

	public static Maybe<String> searchLanguageInFileName(final Pattern fileNamePattern, final String textfileName, final int groupNrOfPattern) {
		
		final Maybe<String> maybeGroupFound = getMatchingGroup(fileNamePattern, textfileName, groupNrOfPattern);
		if (maybeGroupFound.isJust()) {
			try {
				return Maybe.just(maybeGroupFound.getValue());
			} catch (NumberFormatException ex) {
				log.warn(String.format("Unable to parse the year (pattern group '%d') of file '%s'", textfileName, groupNrOfPattern));
			}
		}
		return Maybe.nothing();
	}
	
	public static Maybe<Integer> searchYearInFileName(final Pattern fileNamePattern, final String textfileName, final int groupNrOfPattern) {
		
		final Maybe<String> maybeGroupFound = getMatchingGroup(fileNamePattern, textfileName, groupNrOfPattern);
		if (maybeGroupFound.isJust()) {
			try {
				return Maybe.just(Integer.parseInt(maybeGroupFound.getValue()));
			} catch (NumberFormatException ex) {
				log.warn(String.format("Unable to parse the year (pattern group '%d') of file '%s'", groupNrOfPattern, textfileName));
			}
		}
		return Maybe.nothing();
	}

	public static Maybe<String> getMatchingGroup(
			Pattern fileNamePattern, String textfileName, int groupNrOfPattern) {
		final Matcher matcher = fileNamePattern.matcher(textfileName);
		if (matcher.matches() && matcher.group(groupNrOfPattern) != null)
			return Maybe.just(matcher.group(groupNrOfPattern));
		return Maybe.nothing();
	}
		

	private String language = null;

	private boolean languageErrorOccurred;
	
	public String getLanguage() {
		if (!languageErrorOccurred && this.language == null) {
			final Maybe<String> maybeInputLanguage = searchLanguageInFileName(inputFileNamePattern, this.getTextfileName(), 4);
			final Maybe<String> maybeOutputLanguage = searchLanguageInFileName(outputFileNamePattern, this.getTextfileName(), 1);
			if (maybeInputLanguage.isJust()) {
				this.language = maybeInputLanguage.getValue();
			} else if (maybeOutputLanguage.isJust()) {
				this.language = maybeOutputLanguage.getValue();
			} else {
				this.language = null;
				languageErrorOccurred = true;
				log.warn(String.format("[%s]: unable to find language of this textfile, because the used file name pattern ('%s') did not match '%s'.", inputFileNamePattern.toString()+" and "+outputFileNamePattern.toString(), this.getTextfileName()));
			}
			
			if (log.isInfoEnabled() && !languageErrorOccurred) {
				log.info(String.format("[%s]: initialized language value '%s'", this.getTextfileName(), this.language));
			}
		}
		return this.language;
	}

	public TextfileType getOutputType() {
		return this.textFileType;
	}

	public File getFile() throws FileNotFoundException {
		if (this.inputFile == null || !this.inputFile.exists() || !this.inputFile.isFile())
			throw new FileNotFoundException("The assigned parameter points to null or does not exist as expected.");
		return this.inputFile;
	}

	public Source getNext() throws ReachedEndException {
		if (this.sourcePool == null) { 
			throw new ReachedEndException(String.format("Instance of class %s not even initialized. Therefore no object can be generated from this %s.", SourceInputstreamPool.class.getSimpleName(), SelectorPool.class.getSimpleName()));
		}
		return this.sourcePool.acquire();
	}

	public int getNumberOfSources() {
		if (this.sourcePool != null && this.sourcePool.finished()) { return this.sourcePool.size(); }
		try {
			return this.sourceNumberFinder.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public synchronized void release(Source source) {
		this.sourcePool.release(source);
	}

	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(this.getTextfileName());
		sb.append(" {name: '");
		sb.append(this.getTextfileName());
		sb.append("', language: '");
		sb.append(this.getLanguage());
		if (this.getOutputType() != null) {
			sb.append("', type: '");
			sb.append(this.getOutputType().getOutputName());
		}
		sb.append("', year: '");
		sb.append(this.getYear());
		sb.append("', number of sources: '");
		sb.append(this.getNumberOfSources());
		sb.append("'}");
		return sb.toString();
		
	}
	
	public void release() {
		this.sourcePool = null;
		this.textFileType = null;
		this.inputFile = null;
	}

	public static class SourceNumberFinder implements Callable<Integer> {

		private static final Logger log = LoggerFactory.getLogger(SourceNumberFinder.class);
		
		private File file;

		public SourceNumberFinder(final File inputFile) throws FileNotFoundException {
			
			if (!inputFile.exists()) { throw new FileNotFoundException(String.format("Unable to find file '%s'. Therefore unable to count the number of sources in file.", inputFile.getAbsolutePath())); }
			
			this.file = inputFile;
			
			log.info(String.format("Initialized new instance of class '%s'. Trying to find the number of sources of file '%s'", SourceNumberFinder.class.getSimpleName(), inputFile.getName()));
			
		}

		@Override
		public Integer call() throws Exception {
			BufferedReader reader = null;
			AtomicInteger count = new AtomicInteger(0);
			try {
				reader = new BufferedReader(new FileReader(this.file));
				String line = null;
				while ((line = reader.readLine()) != null) {
					
					if (log.isDebugEnabled() && count.get() + 1 % 1000 == 0) {
						log.debug(String.format("Already '%d' sources found in file '%s'"));
					}
					
					if (line.startsWith(SourceInputstreamPool.SOURCE_START)) {
						count.incrementAndGet();
					}
				}
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
			
			return count.get();
			
		}
		
	}
	
}
