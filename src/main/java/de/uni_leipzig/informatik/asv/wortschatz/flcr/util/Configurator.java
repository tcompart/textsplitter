package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import de.uni_leipzig.asv.clarin.common.io.PropertyReader;
import de.uni_leipzig.asv.clarin.common.io.exception.EntryNotFoundException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;

public class Configurator {

	public static final String PROPERTY_YEAR = "flcr.year";
	public static final String PROPERTY_BASE_OUTPUT = "flcr.output.base";
	public static final String PROPERTY_FINDLINKS = "flcr.fl.name";
	public static final String PROPERTY_WEBCRAWL = "flcr.cr.name";

	public static final String PROPERTY_SPLIT_PREFIX = "flcr.split";
	public static final String PROPERTY_LANGUAGE_DOMAIN_PREFIX = PROPERTY_SPLIT_PREFIX + ".ld";
	public static final String PROPERTY_BASE_LANGUAGE_ALLOWED = "flcr.filter.sourcefilter.base.language.allow";
	public static final String PROPERTY_BASE_FILE_EXTENSION = PROPERTY_BASE_OUTPUT + ".file.extension";
	public static final String PROPERTY_LANGUAGE_LIST_NAME = PROPERTY_SPLIT_PREFIX + ".configuration.name";

	public static final Integer DEFAULT_YEAR = 2012;
	public static final String DEFAULT_BASE_OUTPUT_DIRECTORY = "/tmp";
	public static final String DEFAULT_BASE_FILE_EXTENSION = ".txt";
	public static final String DEFAULT_LANGUAGE_LIST_NAME = "languagedomain.conf";

	private static Configurator configurator;

	private final Integer year;
	private final String baseOutputDirectory;
	private final String baseFileExtension;
	private final String textfileLanguageListName;

	public Configurator() {
		year = DEFAULT_YEAR;
		baseOutputDirectory = DEFAULT_BASE_OUTPUT_DIRECTORY;
		baseFileExtension = DEFAULT_BASE_FILE_EXTENSION;
		textfileLanguageListName = DEFAULT_LANGUAGE_LIST_NAME;
	}

	public Configurator(Properties properties) {

		this.year = (properties.containsKey(PROPERTY_YEAR) ? Integer.parseInt(properties.getProperty(PROPERTY_YEAR))
				: DEFAULT_YEAR);
		this.baseOutputDirectory = (properties.containsKey(PROPERTY_BASE_OUTPUT) ? properties
				.getProperty(PROPERTY_BASE_OUTPUT) : DEFAULT_BASE_OUTPUT_DIRECTORY);
		this.baseFileExtension = (properties.containsKey(PROPERTY_BASE_FILE_EXTENSION) ? properties
				.getProperty(PROPERTY_BASE_FILE_EXTENSION) : DEFAULT_BASE_FILE_EXTENSION);
		this.textfileLanguageListName = (properties.containsKey(PROPERTY_LANGUAGE_LIST_NAME) ? properties
				.getProperty(PROPERTY_LANGUAGE_LIST_NAME) : DEFAULT_LANGUAGE_LIST_NAME);
	}

	public Configurator(File propertyFile) throws FileNotFoundException {
		PropertyReader reader = new PropertyReader(propertyFile);

		Integer temporaryYear = 0;
		try {
			Integer value = Integer.parseInt(reader.getProperty(PROPERTY_YEAR));
			if (value != null && value > 0) {
				temporaryYear = value;
			}
		} catch (EntryNotFoundException e) {
			// ignore -> catched
		}
		year = (temporaryYear != 0 ? temporaryYear : DEFAULT_YEAR);

		String temporaryBaseDirectory = null;
		try {
			temporaryBaseDirectory = reader.getProperty(PROPERTY_BASE_OUTPUT);
		} catch (EntryNotFoundException e) {
			// ignore -> catched
		}
		this.baseOutputDirectory = (temporaryBaseDirectory != null ? temporaryBaseDirectory
				: DEFAULT_BASE_OUTPUT_DIRECTORY);

		String temporaryBaseLanguageAllowedProperty = null;
		try {
			temporaryBaseLanguageAllowedProperty = reader.getProperty(PROPERTY_BASE_LANGUAGE_ALLOWED);
		} catch (EntryNotFoundException e) {
			// ignore -> catched
		}

		String temporaryBaseOutputFileExtension = null;
		try {
			temporaryBaseOutputFileExtension = reader.getProperty(PROPERTY_BASE_FILE_EXTENSION);
		} catch (EntryNotFoundException e) {
			// ignore -> catched
		}
		this.baseFileExtension = (temporaryBaseOutputFileExtension != null ? temporaryBaseOutputFileExtension
				: DEFAULT_BASE_FILE_EXTENSION);

		String temporaryTextfileLanguageDomainFileName = null;
		try {
			temporaryTextfileLanguageDomainFileName = reader.getProperty(PROPERTY_LANGUAGE_LIST_NAME);
		} catch (EntryNotFoundException ex) {
			// ignore -> catched
		}
		this.textfileLanguageListName = (temporaryTextfileLanguageDomainFileName != null ? temporaryTextfileLanguageDomainFileName
				: DEFAULT_LANGUAGE_LIST_NAME);

	}

	public Configurator(Configurator inputConfigurator) {
		this.year = inputConfigurator.getYear();
		this.baseOutputDirectory = inputConfigurator.getBaseOutputDirectory();
		this.baseFileExtension = inputConfigurator.getDefaultFileExtension();
		this.textfileLanguageListName = inputConfigurator.getTextfileLanguageListFileName();
	}

	public static Configurator getConfiguration() {
		if (configurator == null) {
			configurator = new Configurator();
		}
		return configurator;
	}

	public static void setConfiguration(Configurator inputConfigurator) {
		configurator = inputConfigurator;
	}

	public Integer getYear() {
		return this.year;
	}

	public boolean includeYear() {
		return this.getYear() > 0 && this.getYear() != Textfile.DEFAULT_YEAR;
	}

	public String getBaseOutputDirectory() {
		return this.baseOutputDirectory;
	}

	@Override
	public boolean equals(Object that) {

		if (this == that) { return true; }

		if (that == null || that.getClass() != this.getClass()) { return false; }

		Configurator obj = (Configurator) that;

		return this.baseOutputDirectory.equals(obj.baseOutputDirectory) && this.year.equals(obj.year);
	}

	@Override
	public int hashCode() {

		int hashCode = 23;
		final int multiPrim = 37;

		hashCode += hashCode * multiPrim + this.getYear().hashCode();
		hashCode += hashCode * multiPrim + this.getBaseOutputDirectory().hashCode();
		if (this.includeYear()) {
			hashCode += hashCode * multiPrim + 2;
		}
		hashCode += hashCode * multiPrim + this.getDefaultFileExtension().hashCode();
		hashCode += hashCode * multiPrim + this.getTextfileLanguageListFileName().hashCode();
		return hashCode;
	}

	public String getDefaultFileExtension() {
		return this.baseFileExtension;
	}

	public String getTextfileLanguageListFileName() {
		return this.textfileLanguageListName;
	}
}
