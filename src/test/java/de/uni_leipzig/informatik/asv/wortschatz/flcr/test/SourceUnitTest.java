package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;

public class SourceUnitTest {

	private static final String sourceString = "<source><location>http://dda.mty.itesm.mx/romulogarza/comite.html</location><date>2011-02-02</date><user>Treasurer</user><original_encoding>iso-8859-1</original_encoding><language>spa</language></source>";

	private static File inputFile;

	@BeforeClass
	public static void setUpClass() throws IOException {
		Resource resource = new ClassPathResource("Unigramm/FL_spa_limited0000.txt");

		assertThat(resource.exists(), is(true));

		inputFile = resource.getFile();

	}

	private Source source;

	@Before
	public void setUp() {
		source = new Source(sourceString, new StringBuffer());
	}

	@Test
	public void create() {
		assertThat(source, notNullValue());
		assertThat(source.toString(), is(sourceString));
		assertThat(source.getLanguage(), is("spa"));
		assertThat(source.getLocation(), notNullValue());
		assertThat(source.getContent().length(), is(0));
	}
	
	@Test(expected=NullPointerException.class)
	public void createNull1() {
		new Source(null, new StringBuffer());
	}
	
	@Test(expected=NullPointerException.class)
	public void createNull2() {
		new Source(sourceString, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createIllegal1() {
		new Source("", new StringBuffer());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createIllegal2() {
		new Source("<source></source>", new StringBuffer());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createIllegal3() {
		new Source("<source><location></location></source>", new StringBuffer());
	}
	
	@Ignore("This test has to be ignored, two sources, which are equals, have to be a different hash code, because of the source input stream pool.")
	@Test
	public void sameSource() {
		
		assertThat(new StringBuffer().toString(), is(new StringBuffer().toString()));
		
		final Source toCompare = new Source(sourceString, new StringBuffer());

		assertThat(source.equals(toCompare), is(true));

	}
}
