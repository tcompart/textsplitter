package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;

public class ConfiguratorUnitTest {

	private static final String PRE_DEFINED_PROPERTY_FILE = "propertyFile.properties";

	public static final String PROPERTY_FILE_BASE_DIRECTORY = "/temporaryDirectory";

	private static final Integer PROPERTY_FILE_YEAR = 2013;
	
	@BeforeClass
	public static void setUpPropertyFile() throws IOException {
		File file = new File(PRE_DEFINED_PROPERTY_FILE);
		
		assertThat(file.exists(), is(false));
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(String.format("%s=%s", Configurator.PROPERTY_BASE_OUTPUT, PROPERTY_FILE_BASE_DIRECTORY));
			writer.newLine();
			writer.write(String.format("%s=%d", Configurator.PROPERTY_YEAR, PROPERTY_FILE_YEAR));
			writer.newLine();
		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
		
		assertThat(file.exists(), is(true));		
	}
	
	@AfterClass
	public static void tearDownPropertyFile() {
		File file = new File(PRE_DEFINED_PROPERTY_FILE);
		
		if (file.exists()) {
			file.delete();
		}
		
		assertThat(file.exists(), is(false));
	}
	
	@Test
	public void create() {
		// default constructor
		Configurator configuration = Configurator.getConfiguration();
		assertThat(configuration, notNullValue());
		assertThat(configuration.getBaseOutputDirectory(), is(Configurator.DEFAULT_BASE_OUTPUT_DIRECTORY));
		
		assertThat(configuration.includeYear(), is(true));
		assertThat(configuration.getYear(), is(Configurator.DEFAULT_YEAR));
		assertThat(Configurator.DEFAULT_YEAR, not(0));
		
		assertThat(configuration.isGeneralLanguagePatternAllowed(), is(false));
	}
	
	@Test
	public void sameConfigurator() {
		Configurator defaultConfiguration = new Configurator();
		Configurator differentDefaultConfiguration = new Configurator();
		
		assertThat(defaultConfiguration, notNullValue());
		assertThat(differentDefaultConfiguration, notNullValue());
		assertThat(defaultConfiguration, is(differentDefaultConfiguration));
	}
	
	@Test
	public void sameConfigurator2() {
		
		Properties properties = new Properties();
		final String TEMPORARY_DIRECTORY_NAME = "/newtemporaryDirectoryName";
		properties.setProperty(Configurator.PROPERTY_BASE_OUTPUT, TEMPORARY_DIRECTORY_NAME);
		Configurator newConfigurator = new Configurator(properties);
		
		Configurator.setConfiguration(newConfigurator);
		
		assertThat(Configurator.getConfiguration(), is(newConfigurator));
		assertThat(Configurator.getConfiguration().getBaseOutputDirectory(), is(TEMPORARY_DIRECTORY_NAME));
		
	}
	
	@Test
	public void createFromFile() throws FileNotFoundException {
		
		
		File propertyFile = new File(PRE_DEFINED_PROPERTY_FILE);
		Configurator configurator = new Configurator(propertyFile);
		
		assertThat(configurator, notNullValue());
		assertThat(configurator.includeYear(), is(true));
		assertThat(configurator.getYear(), is(PROPERTY_FILE_YEAR));
		assertThat(configurator.getBaseOutputDirectory(), is(PROPERTY_FILE_BASE_DIRECTORY));
		
	}
	
}
