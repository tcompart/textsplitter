package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configurator {

	public static final String PROPERTY_YEAR = "flcr.year";

	public static final String PROPERTY_BASE_OUTPUT = "flcr.output.base";
	public static final String PROPERTY_BASE_FILE_EXTENSION = PROPERTY_BASE_OUTPUT + ".file.extension";

	public static final String PROPERTY_SPLIT_PREFIX = "flcr.split";
	public static final String PROPERTY_SPLIT_SIZE = PROPERTY_SPLIT_PREFIX + ".size";
	public static final String PROPERTY_LANGUAGE_LIST_NAME = PROPERTY_SPLIT_PREFIX + ".configuration.name";

	public static final long DEFAULT_SPLIT_SIZE = 0;
	public static final Integer DEFAULT_YEAR = 2012;
	public static final String DEFAULT_BASE_OUTPUT_DIRECTORY = "/tmp";
	public static final String DEFAULT_BASE_FILE_EXTENSION = ".txt";
	public static final String DEFAULT_LANGUAGE_LIST_NAME = "languagedomain.conf";

	private static Configurator configurator;

	public static Configurator getGlobalConfiguration() {
		if ( configurator == null ) {
			configurator = new Configurator();
		}
		return configurator;
	}

	public static void setGlobalConfiguration( Configurator inputConfigurator ) {
		configurator = inputConfigurator;
	}

	private final Properties properties;

	public Configurator() {
		this( new Properties() );
	}

	public Configurator( Properties properties ) {
		this.properties = properties;
	}

	public Configurator( File propertyFile ) throws IOException {
		this( new Properties() );
		properties.load( new FileInputStream( propertyFile ) );
	}

	public Configurator( Configurator inputConfigurator ) {
		this.properties = inputConfigurator.properties;
	}

	public Integer getYear() {
		return ( properties.containsKey( PROPERTY_YEAR ) ? Integer.parseInt( properties.getProperty( PROPERTY_YEAR ) ) : DEFAULT_YEAR );

	}

	public boolean includeYear() {
		return this.getYear() > 0 && !this.getYear().equals( TextFile.DEFAULT_YEAR );
	}

	public String getBaseOutputDirectory() {
		return ( properties.containsKey( PROPERTY_BASE_OUTPUT ) ? properties.getProperty( PROPERTY_BASE_OUTPUT ) : DEFAULT_BASE_OUTPUT_DIRECTORY );
	}

	public String getDefaultFileExtension() {
		return ( properties.containsKey( PROPERTY_BASE_FILE_EXTENSION ) ? properties.getProperty( PROPERTY_BASE_FILE_EXTENSION ) : DEFAULT_BASE_FILE_EXTENSION );
	}

	public long getDefaultSplitSize() {
		return ( properties.containsKey( PROPERTY_SPLIT_SIZE ) ? Long.parseLong( properties.getProperty( PROPERTY_SPLIT_SIZE ) ) : DEFAULT_SPLIT_SIZE );
	}

	public String getTextFileLanguageListFileName() {
		return ( properties.containsKey( PROPERTY_LANGUAGE_LIST_NAME ) ? properties.getProperty( PROPERTY_LANGUAGE_LIST_NAME ) : DEFAULT_LANGUAGE_LIST_NAME );
	}
}
