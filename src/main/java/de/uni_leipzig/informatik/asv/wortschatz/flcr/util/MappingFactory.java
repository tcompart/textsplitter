package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

import de.uni_leipzig.asv.clarin.common.tuple.Pair;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextfileType;

public class MappingFactory {
	
	public static final String DEFAULT_DIVISION_SIGN = "_";
	
	// deu_limited_webfl_2012_0000.txt
	// deu_limited_webfl.txt
	//
	public static final Pattern pattern = Pattern.compile("(([a-zA-Z]{3}(_[a-zA-Z0-9]+)*)(_web(fl|cr)))(_\\d{4}(_\\d{4})?)?\\.txt");
	
	private static final Logger log = LoggerFactory.getLogger(MappingFactory.class);
	
	private String DIVISION_SIGN = DEFAULT_DIVISION_SIGN;
	
	private final Configurator configurator;

	private Predicate<Pair<String, String>> textfileLanguageFilter = null;

	public MappingFactory() {
		this.configurator = Configurator.getConfiguration();
	}

	public MappingFactory(final Configurator inputConfigurator) {
		
		if (inputConfigurator == null) {
			throw new NullPointerException();
		}
		
		this.configurator = inputConfigurator;
	}

	public Configurator getConfigurator() {
		return this.configurator;
	}
	
	public void setDivisionSign(final String inputDivisionSign) {
		
		if (inputDivisionSign == null) {
			throw new NullPointerException();
		}
		
		DIVISION_SIGN = inputDivisionSign;
	}

	private String getDefaultFileName(final Textfile textfile) {
		final String fileName = textfile.getLanguage() + DIVISION_SIGN + this.getTextfileType(textfile);

		return fileName;
	}

	public String getTextfileType(final Textfile textfile) {
		return textfile.getOutputType().getOutputName();
	}
	
	public String getYear(final Textfile textfile) {
		if (getConfigurator() != null && getConfigurator().includeYear()) {
			final Integer year = (textfile.getYear() != Textfile.DEFAULT_YEAR ? textfile.getYear() : getConfigurator().getYear());
			return String.format("%s%d", DIVISION_SIGN, year);
		}
		return "";
	}

	public String getDefaultFileExtension() {
		
		final String fileExtension = getConfigurator().getDefaultFileExtension();
		
		if (fileExtension.startsWith(".")) {
			return fileExtension;
		}
		return String.format(".%s", fileExtension);
	}

	public File geDefaultParentDirectories(Textfile textfile) {
		return new File(this.getDefaultOutputDirectory(textfile.getOutputType()), 
				this.getDefaultFileName(textfile));
	}

	public File getDefaultOutputDirectory(TextfileType outputType) {

		if (outputType == null) { throw new NullPointerException(String.format("An initiliazed value enum '%s' was expected.", TextfileType.class.getName())); }
		// this should to be : findlinks or webcrawl.... (some other long and verbose name)
		final String outputDirectoryName = outputType.toString();

		if (this.configurator != null) {
			final String baseOutputDirectory = this.configurator.getBaseOutputDirectory();
			if (baseOutputDirectory != null) { 
				return new File(baseOutputDirectory, outputDirectoryName); 
			}
		}
		return new File(outputDirectoryName);
	}

	public Predicate<Pair<String,String>> getLanguageFilter() {
		if (textfileLanguageFilter == null) {
			textfileLanguageFilter = new TextfileLanguageFilter(this.getConfigurator());
		}
		return textfileLanguageFilter;
	}
	
	public void setLanuageFilter(final Predicate<Pair<String,String>> inputLanguageFilter) {
		this.textfileLanguageFilter = inputLanguageFilter;
	}
	
	public boolean isSupportedSourceLanguage(final Textfile inputTextfile, final Source inputSource) {

		if (inputTextfile == null || inputSource == null) { throw new NullPointerException(); }

		final boolean booleanResult;
		
		if (inputSource.getLocation() == null) {
			booleanResult = false;
		} else {
			booleanResult = this.getLanguageFilter().apply(Pair.create(inputTextfile.getLanguage(), inputSource.getLocation().getDomain()));
		}
		log.info(String.format("%s '%s' with %s '%s' %s supported by language filter '%s' ", Textfile.class.getSimpleName(), inputTextfile, Source.class.getSimpleName(), inputSource, (booleanResult ? "is" : "is not"), this.getLanguageFilter()));
		
		return booleanResult;
	}

	public File getDefaultTextfileMapping(Textfile textfile) {
		return this.newFile(textfile,new File(this.geDefaultParentDirectories(textfile), 
				this.getDefaultFileName(textfile) + 
				this.getYear(textfile) + 
				this.getDefaultFileExtension()
			), this.getConfigurator().getDefaultSplitSize());
	}

	public File getSourceDomainMapping(final Textfile textfile, final Source source) {

		if (!this.isSupportedSourceLanguage(textfile, source)) { 
			log.warn("Textfile '{}' (lanuage: {}) with source '{}' are not qualified for a source domain mapping. Mapping the source the default output.", new Object[]{ textfile.getTextfileName(), textfile.getLanguage(), source.toString() });
			return this.getDefaultTextfileMapping(textfile); 
		}
		log.info("Mapping Textfile '{}' (language: {}) with source '{}' to default source domain output, depending on domain '{}'", new Object[]{ textfile.getTextfileName(), textfile.getLanguage(), source, source.getLocation().getDomain()});
		return this.newFile(textfile, new File(this.geDefaultParentDirectories(textfile), 
					textfile.getLanguage() + DIVISION_SIGN +
					source.getLocation().getDomain() + DIVISION_SIGN + 
					this.getTextfileType(textfile) + 
					this.getYear(textfile) + 
					this.getDefaultFileExtension()
				), this.getConfigurator().getDefaultSplitSize());

	}
	
	public File newFile(final Textfile textfile, final File inputFile, final long fileSplitSize) {
		
		if (fileSplitSize == Configurator.DEFAULT_SPLIT_SIZE) {
			return inputFile;
		}
		
		final String fileName = inputFile.getName();
		final Pattern fileNamePrefixPattern = Pattern.compile("(([a-zA-Z_-]+)(\\d{4})?(_(\\d{4}))?)");
		
		Matcher matcher = fileNamePrefixPattern.matcher(fileName);
		
		String fileNamePrefix;
		final int numberToIncrement;
		final String fileNameSuffix = this.getDefaultFileExtension();
		
		final String year;
		if (this.getYear(textfile).isEmpty()) {
			year = null;
		} else if (this.getYear(textfile).startsWith(DIVISION_SIGN)) {
			year = this.getYear(textfile).substring(1);
		} else {
			year = this.getYear(textfile);
		}
		
		if (matcher.find()) {
			fileNamePrefix = matcher.group(2);
			
			// no number exists (no year, no incrementable number)
			if (matcher.group(3) == null || matcher.group(3).isEmpty()) {
				numberToIncrement = 0;
			// if the first number found, is equals the year
				// that is really ugly... what can be done?! the getYear function returns a '_YEAR' string or is empty ''.
			} else if (year != null && matcher.group(3).equals(year)) {
				// then append the year, and DIVISION SIGN
				fileNamePrefix += matcher.group(3) + DIVISION_SIGN;
				
				// first one, the second number exists also: increment the second number
				if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
					numberToIncrement = Integer.parseInt(matcher.group(5));
				// second: no second number exists, therefore initialize a second number by 0
				} else {
					numberToIncrement = 0;
				}
			// last and actually also the least: the found number is not the year: therefore increment by 1
			// TODO probably BUG AREA NR 1: because, how do we know the year? what about, old files like 2011, which should be addressed now 2012... 
			} else {
				numberToIncrement = Integer.parseInt(matcher.group(3));
			}
		} else {
			log.error("The file name prefix pattern did not find any group of input file name: {}. Returning the assigned input file as it is, without any changes.", inputFile.getName());
			return inputFile;
		}
		
		// build everything together
		final String splitNumberString;
		if (inputFile.length() >= fileSplitSize * 1024) {
			splitNumberString = String.format("%04d", numberToIncrement + 1);
		} else {
			splitNumberString = String.format("%04d", numberToIncrement);
		}
		
		File newFile = new File(inputFile.getParent(), fileNamePrefix + splitNumberString + fileNameSuffix); 
		
		// if the expected file is also already full... try recursivly...
		if (newFile.exists() && newFile.length() >= fileSplitSize * 1024) {
			return this.newFile(textfile, newFile, fileSplitSize);
		}
		return newFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((DIVISION_SIGN == null) ? 0 : DIVISION_SIGN.hashCode());
		result = prime * result
				+ ((configurator == null) ? 0 : configurator.hashCode());
		result = prime
				* result
				+ ((textfileLanguageFilter == null) ? 0
						: textfileLanguageFilter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingFactory other = (MappingFactory) obj;
		if (DIVISION_SIGN == null) {
			if (other.DIVISION_SIGN != null)
				return false;
		} else if (!DIVISION_SIGN.equals(other.DIVISION_SIGN))
			return false;
		if (configurator == null) {
			if (other.configurator != null)
				return false;
		} else if (!configurator.equals(other.configurator))
			return false;
		if (textfileLanguageFilter == null) {
			if (other.textfileLanguageFilter != null)
				return false;
		} else if (!textfileLanguageFilter.equals(other.textfileLanguageFilter))
			return false;
		return true;
	}


	
}
