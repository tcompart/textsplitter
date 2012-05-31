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
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;

public class TextfileIntegrationTest extends TextfileUnitTest {

	private static Logger log = LoggerFactory.getLogger(TextfileIntegrationTest.class);
	
	@Test
	public void compareTextfile() throws IOException {
		Textfile textfile = new Textfile(file1);

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
			for (Source source = textfile.getNext(); source != null; source = textfile.getNext()) {
				sourceCounter++;
				log.info(String.format("Checking next source: '%s'", source.toString()));

				/*
				 * get content of every source instance
				 */
				final String sourceAddress = source.toString();
				
				assertThat(source.getContent().indexOf(sourceAddress), is(0));
				assertThat(source.getContent().indexOf("\n") == sourceAddress.length(), is(true));
				
				allSourcesStringBuffer.append(source.getContent());
				
			}
			fail("Reached the end of sources.");
		} catch (ReachedEndException ex) {
			// ignore
		}
		assertThat(sourceCounter, is(textfile.getNumberOfSources()));
		
		final String assertionMsg = String.format("%d and %d do not match!", allSourcesStringBuffer.length(), stringBufferToBeCompared.length());		
		assertThat(assertionMsg, allSourcesStringBuffer.toString(), is(stringBufferToBeCompared.toString()));
		
	}
	
}
