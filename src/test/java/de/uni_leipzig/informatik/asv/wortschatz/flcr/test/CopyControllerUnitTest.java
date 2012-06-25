package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.CopyController;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextfileType;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public class CopyControllerUnitTest {

	protected static File textfileFile;

	protected static final MappingFactory factory = new MappingFactory(Configurator.getConfiguration());

	protected CopyController controller;

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
		final File findLinksDirectory = factory.getDefaultOutputDirectory(TextfileType.Findlinks);
		final File webcrawlDirectory = factory.getDefaultOutputDirectory(TextfileType.Webcrawl);
		
		assertThat(IOUtil.removeDirectory(findLinksDirectory), is(true));
		assertThat(IOUtil.removeDirectory(webcrawlDirectory), is(true));

		assertThat(IOUtil.createDirectory(findLinksDirectory), is(true));
		assertThat(IOUtil.createDirectory(webcrawlDirectory), is(true));
		
		assertThat(findLinksDirectory.exists(), is(true));
		assertThat(findLinksDirectory.isDirectory(), is(true));
		assertThat(webcrawlDirectory.exists(), is(true));
		assertThat(webcrawlDirectory.isDirectory(), is(true));
		
		controller = new CopyController(new Configurator());
	}

	@After
	public void tearDown() {
		final File findLinksDirectory = factory.getDefaultOutputDirectory(TextfileType.Findlinks);
		final File webcrawlDirectory = factory.getDefaultOutputDirectory(TextfileType.Webcrawl);
		
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
		assertThat(controller.isStoped(), is(true));
	}

	@Test(expected = IllegalStateException.class)
	public void copyIllegalState() {
		this.create();

		controller.run();
	}

	@Test(expected = NullPointerException.class)
	public void copyNullFileSet() {
		this.create();

		controller.start(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void copyEmptyFileSet() {

		this.create();

		controller.start(new HashSet<File>());
		/*
		 * an empty hash set can be finished very fast...
		 * maby this method fails, because the thread is to slow
		 * to finish... Thread.sleep(1) should help
		 */
		assertThat(controller.isRunning(), is(false));
		assertThat(controller.isStoped(), is(true));
	}

}
