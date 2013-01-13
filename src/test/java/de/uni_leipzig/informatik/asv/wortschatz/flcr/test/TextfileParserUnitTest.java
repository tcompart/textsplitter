package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFileType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;

public class TextfileParserUnitTest {

	private static final Pattern sourcePattern = Pattern.compile("^<source><location>");

	private static Integer numberOfSources = 0;

	private static File file = null;

	@BeforeClass
	public static void setUpClass() throws IOException {
		ClassPathResource resource = new ClassPathResource("Unigramm/FL_spa_limited0000.txt");
		assertThat(resource.exists(), is(true));
		assertThat(resource.getFile(), notNullValue());

		file = resource.getFile();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				Matcher matcher = sourcePattern.matcher(line);
				if (matcher.find()) {
					numberOfSources++;
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		assertThat(numberOfSources, not(0));

	}

	private TextFile textFile;

	@Before
	public void setUp() throws IOException {
		textFile = new TextFile(file);
	}

	@Test
	public void create() throws FileNotFoundException, ReachedEndException {
		assertThat( textFile, notNullValue());
		assertThat( textFile.getFile(), is(file));
		assertThat( textFile.getOutputType(), is( TextFileType.Findlinks));
		assertThat( textFile.getNext(), notNullValue());
		assertThat( textFile.getLanguage(), notNullValue());
	}

	@Test(expected = ReachedEndException.class)
	public void testNumberOfSources() throws ReachedEndException {
		for (int i = 0; i < numberOfSources; i++) {
			try {
				assertThat( textFile.getNext(), notNullValue());
			} catch (ReachedEndException ex) {
				fail("Here an exception should not be thrown. Therefore the end should not be signaled while requesting all available objects.");
			}
		}
		// here an exception should be thrown
		assertThat( textFile.getNext(), nullValue());
	}

	@Test
	public void getLanguage() {
		assertThat( textFile.getLanguage(), notNullValue());
		assertThat( textFile.getLanguage(), is("spa_limited"));
	}

	@Test
	public void releaseTextfile() {
		Runtime runtime = Runtime.getRuntime();
		// bigger number, because of small free memory number
		Long memoryBefore = runtime.maxMemory() - runtime.freeMemory();

		textFile.release();

		assertThat( textFile, notNullValue());
		try {
			assertThat( textFile.getFile(), nullValue());
			fail("The internal file was released, and atleast the reference should be gone.");
		} catch (FileNotFoundException e) {
			assertThat(e, notNullValue());
		}
		assertThat( textFile.getOutputType(), nullValue());
		try {
			assertThat( textFile.getNext(), nullValue());
			fail("An exception should have be thrown, because the text file and its references were released from memory.");
		} catch (ReachedEndException ex) {
			assertThat(ex, notNullValue());
		}

		// calls garbage collection, which should remove released objects from
		// memory
		runtime.gc();

		// smaller number, because of more free memory
		Long memoryAfter = runtime.maxMemory() - runtime.freeMemory();
		assertThat(memoryBefore - memoryAfter > 0, is(true));
	}

}
