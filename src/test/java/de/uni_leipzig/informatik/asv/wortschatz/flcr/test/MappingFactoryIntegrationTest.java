package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextfileType;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public class MappingFactoryIntegrationTest {

	private static Textfile textfile;
	private static Source source;
	private static File BIG_FILE;
	private MappingFactory mappingFactory;

	@BeforeClass
	public static void setUpResources() throws IOException {
		BIG_FILE = new FileSystemResource("Unigramm/FL_spa0000.txt").getFile();
		textfile = new Textfile(BIG_FILE);
		source = textfile.getNext();
	}
	
	@Before
	public void setUp() {
		mappingFactory = new MappingFactory();
	}
	
	
	@Test
	public void create() {
		
		assertThat(mappingFactory, notNullValue());
		assertThat(mappingFactory.getConfigurator(), notNullValue());
		assertThat(mappingFactory.getLanguageFilter(), notNullValue());
		assertThat(mappingFactory.getDefaultFileExtension(), notNullValue());
	}
	
	@Test(timeout=20)
	public void testTimeGetLanguageFilter() {
		assertThat(mappingFactory.getLanguageFilter(), notNullValue());
	}
	
	private static final Logger log = LoggerFactory.getLogger(MappingFactory.class);
	
	@Test(timeout=20)
	public void testTimeLog() {
		log.info("{} '{}' with {} '{}' {} supported by language filter '{}' ", new Object[]{ Textfile.class.getSimpleName(), "textfile", Source.class.getSimpleName(), source.toString(), (true ? "is" : "is not"), mappingFactory.getLanguageFilter().getClass().getSimpleName()});		
	}
	
	@Test(timeout=20)
	public void testTimeDefaultOutputFindlinks() {
		assertThat(mappingFactory.getDefaultOutputDirectory(TextfileType.Findlinks), notNullValue());
	}
	
	@Test(timeout=20)
	public void testTimeDefaultOutputWebcrawl() {
		assertThat(mappingFactory.getDefaultOutputDirectory(TextfileType.Webcrawl), notNullValue());
	}		
	
	@Test(timeout=20)
	public void testTimeDefaultTextfileMapping() {
		assertThat(mappingFactory.getDefaultTextfileMapping(textfile), notNullValue());
	}
		
	@Test(timeout=20)
	public void testTimeDefaultParentDirectories() {
		assertThat(mappingFactory.geDefaultParentDirectories(textfile), notNullValue());
	}
	
	@Test(timeout=20)
	public void testTimeSourceDomainMapping() {
		mappingFactory.getSourceDomainMapping(textfile, source);
	}
	
	@Test(timeout=20)
	public void testTimeIsSupported() {
		assertThat(mappingFactory.isSupportedSourceLanguage(textfile, source), notNullValue());
	}
	
	@Test(timeout=20)
	public void testTimeTextfileType() {
		mappingFactory.getTextfileType(textfile);
	}
	
	@Test(timeout=20)
	public void testTimeGetYear() {
		mappingFactory.getYear(textfile);
	}
	
	
}
