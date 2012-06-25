package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.uni_leipzig.asv.clarin.common.tuple.Pair;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextfileType;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public class MappingFactoryUnitTest {
	
	private static final String DIVISION_SIGN = MappingFactory.DEFAULT_DIVISION_SIGN;
	private static File textfileFile;
	private static Textfile textfile;
	private static Source source;
	private static File tmpLanguageListFile;
	private static Configurator configurator;
	
	@BeforeClass
	public static void setUpClass() throws IOException {
		Resource classPathResource = new ClassPathResource("Unigramm/FL_spa_limited0000.txt");
		assertThat(classPathResource.exists(), is(true));
		
		textfileFile = classPathResource.getFile();
		
		textfile = new Textfile(textfileFile);
		
		source = textfile.getNext();
		
		tmpLanguageListFile = File.createTempFile("temporaryLanguageListFile", "_only_for_testing.txt");
		
		assertThat(tmpLanguageListFile.exists(), is(true));
		
		Properties properties = new Properties();
		properties.put(Configurator.PROPERTY_LANGUAGE_LIST_NAME, tmpLanguageListFile.getAbsolutePath());
		configurator = new Configurator(properties);
		
		assertThat(configurator.getTextfileLanguageListFileName(), is(tmpLanguageListFile.getAbsolutePath()));
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(tmpLanguageListFile));
			writer.write(String.format("%s.%s", textfile.getLanguage(), source.getLocation().getDomain()));
			writer.newLine();
			writer.flush();
		} finally {
			if (writer != null)
				writer.close();
		}
		
	}
	
	@AfterClass
	public static void tearDown() {
		
		textfile.release(source);
		source = null;
		textfile = null;
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
		defaultBaseName = textfile.getLanguage() + DIVISION_SIGN + textfile.getOutputType().getOutputName();
		
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
		final int possible_year = (textfile.getYear() != Textfile.DEFAULT_YEAR ? textfile.getYear() : configurator.getYear());
		assertThat(mf.getYear(textfile), is(String.format("%s%s",DIVISION_SIGN,possible_year)));
	}
	
	@Test
	public void testTextfileOutputType() {
		assertThat(mf.getTextfileType(textfile), is(textfile.getOutputType().getOutputName()));
		
		// assuming, that the MappingFactory uses this configurator;
		for (TextfileType textfileType : TextfileType.values()) {
			assertThat(mf.getDefaultOutputDirectory(textfileType), is(new File(baseDirectory, textfileType.toString())));
		}
	}
	
	@Test
	public void testTextfileMapping() {
		
		assertThat(mf.getDefaultTextfileMapping(textfile), is(new File(baseDirectory + "/" + textfile.getOutputType().toString() + "/" + defaultBaseName, defaultBaseName + mf.getYear(textfile) + configurator.getDefaultFileExtension())));
	}
	
	@Test
	public void testTextfileSourceMapping() {
		
		final String sourceDomain = source.getLocation().getDomain();
		
		assertThat(mf.getLanguageFilter().apply(Pair.create(textfile.getLanguage(), sourceDomain)), is(true));
		assertThat(configurator.getTextfileLanguageListFileName(), is(tmpLanguageListFile.getAbsolutePath()));
		assertThat(new File(configurator.getTextfileLanguageListFileName()).exists(), is(true));
		
		assertThat(MappingFactory.pattern.matcher("deu_webfl_2012.txt").matches(), is(true));
		assertThat(MappingFactory.pattern.matcher("spa_limited_webfl_2012_0000.txt").matches(), is(true));
		
		// the source has to be marked as a new mapping
		assertThat(mf.isSupportedSourceLanguage(textfile, source), is(true)); // otherwise the next assertion will not work
		assertThat(mf.getSourceDomainMapping(textfile, source), is(new File(baseDirectory + "/" + textfile.getOutputType().toString() + "/" + defaultBaseName, textfile.getLanguage() + DIVISION_SIGN + sourceDomain + DIVISION_SIGN + textfile.getOutputType().getOutputName() + mf.getYear(textfile) + configurator.getDefaultFileExtension())));
		
	}
	
	@Test
	public void testEquality() {
		
		MappingFactory mf = new MappingFactory();
		
		assertThat(mf, is(new MappingFactory()));
		
	}
	
}