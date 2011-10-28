/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.unileipzig.asv.wortschatz.flcr.IOUtil;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class PropertyUnitTest {

	private static final File file = new File("properties.prop");
	
	@Before
	public void setUp() throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.setProperty("property", "value");
		prop.store(new FileOutputStream(file), "Properties for project FindLinksWebCrawlerSearch");
	}
	
	@After
	public void tearDown() {
		assertThat(IOUtil.removeFile(file), is(true));
	}
	
	@Test
	public void loadExistingProperties() throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));
		
		assertThat(prop, notNullValue());
		assertThat(prop.size(), is(1));
		assertThat(prop.getProperty("property"), is("value"));
	}
	
	@Test(expected=FileNotFoundException.class)
	public void empty() throws FileNotFoundException, IOException {
		
		Properties prop = new Properties();
		prop.load(new FileInputStream(new File("fileNotExisting.prop")));
	}
}
