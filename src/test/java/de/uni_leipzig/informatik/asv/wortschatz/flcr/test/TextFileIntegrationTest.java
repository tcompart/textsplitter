package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;

public class TextFileIntegrationTest extends TextFileUnitTest {

	private static Logger log = LoggerFactory
			.getLogger(TextFileIntegrationTest.class);

	@Test
	public void compareTextfile() throws IOException {
		TextFile textFile = new TextFile(file1);

		int sourceCounter = 0;

		// string buffer, which should be compared to all sources
		BufferedReader reader = new BufferedReader(new FileReader(file1));
		final StringBuffer stringBufferToBeCompared = new StringBuffer();

		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBufferToBeCompared.append(line);
			stringBufferToBeCompared.append("\n");
		}

		final StringBuffer allSourcesStringBuffer = new StringBuffer();
		try {
			for (Source source = textFile.getNext(); source != null; source = textFile
					.getNext()) {
				sourceCounter++;
				log.info(String.format("Checking next source: '%s'",
						source.toString()));

				/*
				 * get content of every source instance
				 */
				final String sourceAddress = source.toString();

				assertThat(source.getContent().indexOf(sourceAddress), is(0));
				assertThat(
						source.getContent().indexOf("\n") == sourceAddress
								.length(),
						is(true));

				allSourcesStringBuffer.append(source.getContent());

			}
			fail("Reached the end of sources.");
		} catch (ReachedEndException ex) {
			// ignore
		}
		assertThat(sourceCounter, is( textFile.getNumberOfSources()));

		final String assertionMsg = String.format("%d and %d do not match!",
				allSourcesStringBuffer.length(),
				stringBufferToBeCompared.length());
		assertThat(assertionMsg, allSourcesStringBuffer.toString(),
				is(stringBufferToBeCompared.toString()));

	}

	@Test(timeout = 100)
	public void testSpeedGetLanguage() throws IOException {
		TextFile textFile = new TextFile(file1);
		assertThat( textFile.getLanguage(), notNullValue());
	}

	@Test(timeout = 1000)
	public void testSpeedLoadingOfTheFirst20SourcesBig() throws IOException {
		TextFile textFile = new TextFile(file2);
		int sourceCounter = 0;
		try {
			for (Source source = textFile.getNext(); source != null; source = textFile
					.getNext()) {
				sourceCounter++;

				if (sourceCounter >= 60) {
					break;
				}

			}
		} catch (ReachedEndException ex) {
			// ignore
		}

	}

	private static TextFile textFileSMALL;
	static {
		try {
			textFileSMALL = new TextFile(file1);
		} catch (IOException e) {
			textFileSMALL = null;
		}

	}

	@Test(timeout=20)
	public void testTimeToString() {
		textFileSMALL.toString();
	}
	
	/*
	 * strange problem:
	 * textFileSMALL is the smaller one, although it requires more time than the
	 *  problem is here, that the textFileSMALL runs already the SourceInputStreamPool and loads ALL sources
	 * textfileBIG (called only 'file2' here)
	 */
	//TODO fix this phanomean
	@Test(timeout = 1500)
	public void testSpeedLoadingOfTheFirst20SourcesSmall() throws IOException {
		int sourceCounter = 0;
		try {
			for (Source source = textFileSMALL.getNext(); source != null; source = textFileSMALL
					.getNext()) {
				sourceCounter++;

				if (sourceCounter >= 60) {
					break;
				}

			}
		} catch (ReachedEndException ex) {
			// everything ok!!
		}

	}
}
