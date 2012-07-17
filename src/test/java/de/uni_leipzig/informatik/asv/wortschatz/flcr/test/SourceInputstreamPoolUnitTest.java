package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.SourceInputstreamPool;

public class SourceInputstreamPoolUnitTest {

	private static final Logger log = LoggerFactory.getLogger(SourceInputstreamPoolUnitTest.class);
	
	protected Calendar before;
	
	protected static File textfileSmall;
	protected static File textfileBig;
	protected static File textfile;

	@BeforeClass
	public static void setUpClass() throws IOException {
		Resource textfileResource = new ClassPathResource("Unigramm/FL_spa_limited0000.txt");

		assertThat(textfileResource.exists(), is(true));

		textfileSmall = textfileResource.getFile();

		textfileResource = new FileSystemResource("Unigramm/FL_spa0000.txt");

		assertThat(textfileResource.exists(), is(true));

		textfileBig = textfileResource.getFile();

		textfile = textfileBig;
	}

	/*
	 * timeout should be equal for every textfile or source input stream pool:
	 * 
	 * currently: 200 ms, but 100 ms are reached if the files can be loaded
	 * before hand.
	 */
	@Test(timeout = 200)
	public void createSmallPool() throws IOException {
		SourceInputstreamPool pool = new SourceInputstreamPool(textfileSmall);
		assertThat(pool, notNullValue());
	}

	@Test(timeout = 200)
	public void createBigPool() throws IOException {
		SourceInputstreamPool pool = new SourceInputstreamPool(textfileBig);
		assertThat(pool, notNullValue());
	}
	
	@Test(expected=FileNotFoundException.class)
	public void expectFileNotFound() throws IOException {
		new SourceInputstreamPool(new File("cannotbefound.file"));
	}
	
	@Test(expected=NullPointerException.class)
	public void expectNullPointer() throws IOException {
		new SourceInputstreamPool(null);
	}
}
