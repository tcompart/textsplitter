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
		final String fileName = textfile.getLanguage() + DIVISION_SIGN + textfile.getOutputType().getOutputName();

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

	public File getDefaultTextfileMapping(Textfile textfile) {
		return new File(this.geDefaultParentDirectories(textfile), 
					this.getDefaultFileName(textfile) + 
					this.getYear(textfile) + 
					this.getDefaultFileExtension()
				);
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

		boolean booleanResult = this.getLanguageFilter().apply(Pair.create(inputTextfile.getLanguage(), inputSource.getLocation().getDomain()));
		log.info(String.format("%s '%s' with %s '%s' %s supported by language filter '%s' ", Textfile.class.getSimpleName(), inputTextfile, Source.class.getSimpleName(), inputSource, (booleanResult ? "is" : "is not"), this.getLanguageFilter()));
		
		return booleanResult;
	}

	public File getSourceDomainMapping(final Textfile textfile, final Source source) {

		if (!this.isSupportedSourceLanguage(textfile, source)) { return this.getDefaultTextfileMapping(textfile); }
		
		// default file extension should contain a '.' sign ahead, otherwise it may get added here
		return new File(this.geDefaultParentDirectories(textfile), 
					textfile.getLanguage() + DIVISION_SIGN +
					source.getLocation().getDomain() + DIVISION_SIGN + 
					this.getTextfileType(textfile) + 
					this.getYear(textfile) + 
					this.getDefaultFileExtension()
				);

	}
	
	@Override
	public int hashCode() {
		int hashCode = 0;
		
		final int hash = 31;
		final int seed = 13;
		
		hashCode += hash * seed + this.DIVISION_SIGN.hashCode();
		hashCode += hash * seed + this.configurator.hashCode();
		
		return hashCode;
	}
	
	@Override
	public boolean equals(final Object thatObj) {
		
		if (thatObj != null && thatObj instanceof MappingFactory) {
			MappingFactory that = (MappingFactory) thatObj;
			
			return this.DIVISION_SIGN.equals(that.DIVISION_SIGN) &&
					this.configurator.equals(that.configurator);
		}
		
		return false;
	}
	
}
