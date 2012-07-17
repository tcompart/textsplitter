package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.SimpleCopyManager;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;

/*
 * complex:
 * 41 min; 2460 sec; 835 tasks done: 0,34 pro sec OR 2,95 sec pro task
 * 
 * simple:
 * 12 min; 720 sec; 148 tasks done: 0,2 pro sec OR 4,86 sec pro task
 */
public class SimpleCopyManagerIntegrationTest {

	private static final Logger log = LoggerFactory.getLogger(SimpleCopyManagerIntegrationTest.class);
	
	private final File outputDirectory;
	
	{ 
		File temporaryFile = null;
		try {
			temporaryFile = new File(new ClassPathResource(".").getFile(),"tmp");
		} catch (IOException e) {
			temporaryFile = new File("./tmp");
		}
		this.outputDirectory = temporaryFile;
	}


	@Before
	public void setUp() {
		assertThat(this.outputDirectory.exists(), is(false));
		assertThat(this.outputDirectory.mkdirs(), is(true));
	}
	
	@After
	public void tearDown() {
		assertThat(this.outputDirectory.exists(), is(true));
		assertThat(IOUtil.removeDirectory(outputDirectory), is(true));
	}
	
	
	@Test
	public void copy() throws FileNotFoundException {
		
		final File inputDirectory = new FileSystemResource("Unigramm").getFile();
		Properties properties = new Properties();
		properties.put(Configurator.PROPERTY_BASE_OUTPUT, this.outputDirectory.getAbsolutePath());
		
		Configurator configurator = new Configurator(properties);
		SimpleCopyManager copyManager = new SimpleCopyManager(inputDirectory, configurator);
		
		copyManager.start();
		
		assertThat(outputDirectory.exists(), is(true));
		assertThat(outputDirectory.listFiles().length > 0, is(true));
		
		for (File innerOutputDirectory : outputDirectory.listFiles()) {
			assertThat(innerOutputDirectory.isDirectory(), is(true));
			assertThat(innerOutputDirectory.listFiles().length > 0, is(true));
			
			for (File innerFile : innerOutputDirectory.listFiles()) {
				assertThat(innerFile.isFile(), is(true));
				log.info("Found file '{}'", innerFile.getAbsolutePath());
			}
			
		}
		
	}
	
	
}
