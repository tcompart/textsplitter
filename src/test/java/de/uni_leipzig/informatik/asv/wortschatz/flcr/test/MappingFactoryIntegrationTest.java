package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFileType;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

public class MappingFactoryIntegrationTest {

	private static TextFile textFile;
	private static Source source;
	private MappingFactory mappingFactory;

	@BeforeClass
	public static void setUpResources() throws IOException {
		final File bigFile = new ClassPathResource( "Unigramm/FL_spa0000.txt" ).getFile();
		textFile = new TextFile( bigFile );
		source = textFile.getNext();
	}

	@Rule
	public final RuleChain chain = RuleChain.outerRule( new Timeout( 30 ) );

	@Before
	public void setUp() {
		mappingFactory = new MappingFactory();
	}


	@Test
	public void create() {

		assertThat( mappingFactory, notNullValue() );
		assertThat( mappingFactory.getConfigurator(), notNullValue() );
		assertThat( mappingFactory.getLanguageFilter(), notNullValue() );
		assertThat( mappingFactory.getDefaultFileExtension(), notNullValue() );
	}

	@Test
	public void testTimeGetLanguageFilter() {
		assertThat( mappingFactory.getLanguageFilter(), notNullValue() );
	}

	private static final Logger log = LoggerFactory.getLogger( MappingFactory.class );

	@Test
	public void testTimeLog() {
		log.info( "{} '{}' with {} '{}' {} supported by language filter '{}' ", new Object[]{TextFile.class.getSimpleName(), "textFile", Source.class.getSimpleName(), source.toString(), ( true ? "is" : "is not" ), mappingFactory.getLanguageFilter().getClass().getSimpleName()} );
	}

	@Test
	public void testTimeDefaultOutputFindlinks() {
		assertThat( mappingFactory.getDefaultOutputDirectory( TextFileType.Findlinks ), notNullValue() );
	}

	@Test
	public void testTimeDefaultOutputWebcrawl() {
		assertThat( mappingFactory.getDefaultOutputDirectory( TextFileType.Webcrawl ), notNullValue() );
	}

	@Test
	public void testTimeDefaultTextfileMapping() {
		assertThat( mappingFactory.getDefaultTextfileMapping( textFile ), notNullValue() );
	}

	@Test
	public void testTimeDefaultParentDirectories() {
		assertThat( mappingFactory.geDefaultParentDirectories( textFile ), notNullValue() );
	}

	@Test
	public void testTimeSourceDomainMapping() {
		mappingFactory.getSourceDomainMapping( textFile, source );
	}

	@Test
	public void testTimeIsSupported() {
		assertThat( mappingFactory.isSupportedSourceLanguage( textFile, source ), notNullValue() );
	}

	@Test
	public void testTimeTextfileType() {
		mappingFactory.getTextFileType( textFile );
	}

	@Test
	public void testTimeGetYear() {
		mappingFactory.getYear( textFile );
	}


}
