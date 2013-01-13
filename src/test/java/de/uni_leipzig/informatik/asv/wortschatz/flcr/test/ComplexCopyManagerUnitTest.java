package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFileType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.ComplexCopyManager;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public class ComplexCopyManagerUnitTest {

	protected static File textfileFile;

	protected static final MappingFactory factory = new MappingFactory(Configurator.getGlobalConfiguration());

	protected ComplexCopyManager controller;

	@BeforeClass
	public static void setUpClass() throws IOException {
		Resource textfileResource = new ClassPathResource("Unigramm/FL_spa_limited0000.txt");
		assertThat(textfileResource, notNullValue());
		assertThat(textfileResource.exists(), is(true));
		assertThat(textfileResource.getFile(), notNullValue());

		textfileFile = textfileResource.getFile();
	}

	@Before
	public void setUp() throws IOException {
		final File findLinksDirectory = factory.getDefaultOutputDirectory( TextFileType.Findlinks);
		final File webcrawlDirectory = factory.getDefaultOutputDirectory( TextFileType.Webcrawl);
		
		assertThat(IOUtil.removeDirectory(findLinksDirectory), is(true));
		assertThat(IOUtil.removeDirectory(webcrawlDirectory), is(true));

		assertThat(IOUtil.createDirectory(findLinksDirectory), is(true));
		assertThat(IOUtil.createDirectory(webcrawlDirectory), is(true));
		
		assertThat(findLinksDirectory.exists(), is(true));
		assertThat(findLinksDirectory.isDirectory(), is(true));
		assertThat(webcrawlDirectory.exists(), is(true));
		assertThat(webcrawlDirectory.isDirectory(), is(true));
		
		controller = new ComplexCopyManager(textfileFile.getParentFile(), new Configurator());
	}

	@After
	public void tearDown() {
		final File findLinksDirectory = factory.getDefaultOutputDirectory( TextFileType.Findlinks);
		final File webcrawlDirectory = factory.getDefaultOutputDirectory( TextFileType.Webcrawl);
		
		IOUtil.removeDirectory(findLinksDirectory);
		IOUtil.removeDirectory(webcrawlDirectory);
		
		assertThat(findLinksDirectory.exists(), is(false));
		assertThat(findLinksDirectory.isDirectory(), is(false));
		assertThat(webcrawlDirectory.exists(), is(false));
		assertThat(webcrawlDirectory.isDirectory(), is(false));
	}

	@Test
	public void create() {
		assertThat(controller, notNullValue());
		assertThat(controller.isRunning(), is(false));
		assertThat(controller.isStopped(), is(true));
	}

	@Test(expected = NullPointerException.class)
	public void copyNullFileSet() throws FileNotFoundException {
		new ComplexCopyManager(null, null);
	}

	@Ignore
	@Test(expected = IllegalArgumentException.class)
	public void copyEmptyFileSet() throws FileNotFoundException {

		this.create();

		// this may fail because the /tmp directory can contain produced files?!
		controller = new ComplexCopyManager(new File("/tmp"), new Configurator());
		
	}

}
