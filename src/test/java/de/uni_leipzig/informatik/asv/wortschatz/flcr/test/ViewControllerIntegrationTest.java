package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.ViewController;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;

public class ViewControllerIntegrationTest {

	private static String VALID_STATEMENT;
	private static String INVALID_STATEMENT;

	private final static File inputDirectory = new File("Unigramm");
	private final static File outputDirectory = new File("test");

	
	@BeforeClass
	public static void setUpClass() {
		VALID_STATEMENT = String.format("--input %s --output %s", inputDirectory.getAbsolutePath(), outputDirectory.getAbsolutePath());
		INVALID_STATEMENT = "--properties src/main/resources/";
	}

	@Before
	public void setUp() {
		IOUtil.removeDirectory(outputDirectory);
		assertThat(outputDirectory.exists(), is(false));
	}
	
	@After
	public void tearDown() {
		IOUtil.removeDirectory(outputDirectory);
		assertThat(outputDirectory.exists(), is(false));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void expectException() throws IOException, InterruptedException, ExecutionException {
		// missing '--input' property
		ViewController.main(INVALID_STATEMENT.split(" "));
	}
	
	@Ignore("this test takes just tooo long...")
	@Test
	public void inputAndOutputAssigned() throws IOException, InterruptedException, ExecutionException {
		ViewController.main(VALID_STATEMENT.split(" "));
		assertThat(outputDirectory.exists(), is(true));
		assertThat(outputDirectory.isDirectory(), is(true));
		final File[] files = outputDirectory.listFiles();
		assertThat(files == null || files.length > 0, is(true));
	}

}
