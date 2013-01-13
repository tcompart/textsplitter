package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Pair;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFileType;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public class MappingFactoryUnitTest {
	
	private static final String DIVISION_SIGN = MappingFactory.DEFAULT_DIVISION_SIGN;
	private static File textfileFile;
	private static TextFile textFile;
	private static Source source;
	private static File tmpLanguageListFile;
	private static Configurator configurator;
	
	@BeforeClass
	public static void setUpClass() throws IOException {
		Resource classPathResource = new ClassPathResource("Unigramm/FL_spa_limited0000.txt");
		assertThat(classPathResource.exists(), is(true));
		
		textfileFile = classPathResource.getFile();
		
		textFile = new TextFile(textfileFile);
		
		source = textFile.getNext();
		
		tmpLanguageListFile = File.createTempFile("temporaryLanguageListFile", "_only_for_testing.txt");
		
		assertThat(tmpLanguageListFile.exists(), is(true));
		
		Properties properties = new Properties();
		properties.put(Configurator.PROPERTY_LANGUAGE_LIST_NAME, tmpLanguageListFile.getAbsolutePath());
		configurator = new Configurator(properties);
		
		assertThat(configurator.getTextFileLanguageListFileName(), is(tmpLanguageListFile.getAbsolutePath()));
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(tmpLanguageListFile));
			writer.write(String.format("%s.%s", textFile.getLanguage(), source.getLocation().getDomain()));
			writer.newLine();
			writer.flush();
		} finally {
			if (writer != null)
				writer.close();
		}
		
	}
	
	@AfterClass
	public static void tearDown() {
		
		textFile.release(source);
		source = null;
		textFile = null;
		textfileFile = null;
		
		if (tmpLanguageListFile != null && tmpLanguageListFile.exists())
			tmpLanguageListFile.delete();
		assertThat(tmpLanguageListFile.exists(), is(false));
		
	}

	private MappingFactory mf;
	private String baseDirectory;
	private String defaultBaseName;
	

	@Before
	public void setUp() {
		mf = new MappingFactory(configurator);		
		mf.setDivisionSign(DIVISION_SIGN);
		baseDirectory = configurator.getBaseOutputDirectory();
		defaultBaseName = textFile.getLanguage() + DIVISION_SIGN + textFile.getOutputType().getOutputName();
		
	}
	
	@Test
	public void create() {
		assertThat(mf, notNullValue());
		assertThat(mf.getConfigurator(), is(configurator));
	}
	
	@Test
	public void testDefaultFileExtension() {
		assertThat(mf.getDefaultFileExtension(), is(".txt"));
	}
	
	@Test
	public void testYearOfTextfile() {
		final int possible_year = ( textFile.getYear() != TextFile.DEFAULT_YEAR ? textFile.getYear() : configurator.getYear());
		assertThat(mf.getYear( textFile ), is(String.format("%s%s",DIVISION_SIGN,possible_year)));
	}
	
	@Test
	public void testTextfileOutputType() {
		assertThat(mf.getTextFileType( textFile ), is( textFile.getOutputType().getOutputName()));
		
		// assuming, that the MappingFactory uses this configurator;
		for (TextFileType textFileType : TextFileType.values()) {
			assertThat(mf.getDefaultOutputDirectory( textFileType ), is(new File(baseDirectory, textFileType.toString())));
		}
	}
	
	@Test
	public void testTextfileMapping() {
		
		assertThat(mf.getDefaultTextfileMapping( textFile ), is(new File(baseDirectory + "/" + textFile.getOutputType().toString() + "/" + defaultBaseName, defaultBaseName + mf.getYear( textFile ) + configurator.getDefaultFileExtension())));
	}
	
	@Test
	public void testTextfileSourceMapping() {
		
		final String sourceDomain = source.getLocation().getDomain();
		
		assertThat(mf.getLanguageFilter().apply( new Pair<String, String>( textFile.getLanguage(), sourceDomain )), is(true));
		assertThat(configurator.getTextFileLanguageListFileName(), is(tmpLanguageListFile.getAbsolutePath()));
		assertThat(new File(configurator.getTextFileLanguageListFileName()).exists(), is(true));
		
		assertThat(MappingFactory.PATTERN.matcher("deu_webfl_2012.txt").matches(), is(true));
		assertThat(MappingFactory.PATTERN.matcher("spa_limited_webfl_2012_0000.txt").matches(), is(true));
		
		// the source has to be marked as a new mapping
		assertThat(mf.isSupportedSourceLanguage( textFile, source), is(true)); // otherwise the next assertion will not work
		assertThat(mf.getSourceDomainMapping( textFile, source), is(new File(baseDirectory + "/" + textFile.getOutputType().toString() + "/" + defaultBaseName, textFile.getLanguage() + DIVISION_SIGN + sourceDomain + DIVISION_SIGN + textFile.getOutputType().getOutputName() + mf.getYear( textFile ) + configurator.getDefaultFileExtension())));
		
	}
	
	@Test
	public void testEquality() {
		
		MappingFactory mf = new MappingFactory();
		
		assertThat(mf, is(new MappingFactory()));
		
	}
	
}