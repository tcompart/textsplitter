package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.SourceInputstreamPool;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;

public class SourceInputstreamPoolIntegrationTest extends SourceInputstreamPoolUnitTest {

	private static final Logger log = LoggerFactory.getLogger(SourceInputstreamPoolIntegrationTest.class);
	
	@Test
	public void compareNumberOfSourcesOfPoolAndFoundInFile() throws IOException {

		final File file = textfileSmall;

		System.out.println("Starting aquiring new sources.");
		
		int toBeVarifiedNumberOfSources = 0;
		
		final SourceInputstreamPool pool = new SourceInputstreamPool(file);
		
		assertThat(pool.finished(), is(false));
		
		try {
			for (Source source = pool.acquire(); source != null; toBeVarifiedNumberOfSources++, source = pool.acquire()) {
					assertValidSource(source);
					
					if (toBeVarifiedNumberOfSources + 1 % 10 == 0) { // + 1 has to be, because  '0 mod 10000 = 0', which would be plainly unexpected in this case
						System.out.println(String.format("Already %d sources aquired....", toBeVarifiedNumberOfSources));
					}
					assertThat(source.toString(), pool.currentNumber(), is(toBeVarifiedNumberOfSources+1));
					assertThat(source.toString(), source.toString(), is(getNextSource(file,toBeVarifiedNumberOfSources))); // problem with very big files... you can use small files
					
					pool.release(source);
			}
		} catch (ReachedEndException ex) {
			System.out.println(String.format("Finished aquiring every source; number of sources: %d", toBeVarifiedNumberOfSources));
			assertThat(String.format("%d <= %d", pool.currentNumber(), pool.size()), pool.finished(), is(true));
		}
		assertNumberOfSources(pool.size(), file);
	}

	private synchronized String getNextSource(File file, final int expectedNumberOfSource) {
		
		log.debug(String.format("%s: searching for source nr %d", "getNextSource", expectedNumberOfSource));
		
		String result = null;
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(file));
			int counter = 0;
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(SourceInputstreamPool.SOURCE_START)) {
					if (counter == expectedNumberOfSource) {
						result = line;
					}
					counter++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static void assertValidSource(final Source source) {
		
		assertThat(source, notNullValue());
		
		final String sourceString = source.toString();
		
		assertThat(source.getLanguage(), notNullValue());
		
		final String locationSubString = sourceString.substring(sourceString.indexOf(Source.locationStart)+Source.locationStart.length(), sourceString.indexOf(Source.locationEnd));
		if (locationSubString.equalsIgnoreCase("null")) {
			assertThat(locationSubString, source.getLocation(), nullValue());			
		} else {
			assertThat(locationSubString, source.getLocation(), notNullValue());			
		}
		
		assertThat(source.getContent(), notNullValue());
		assertThat(source.getContent().length() > 0, is(true));
		assertThat(source.getContent().indexOf(source.toString()), is(0));
	}

	public static void assertNumberOfSources(final int toBeVarifiedNumberOfSources, final File inputTextfile) {
	
		BufferedReader reader = null;
		int numberOfFoundSources = 0;
		try {
			reader = new BufferedReader(new FileReader(inputTextfile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(SourceInputstreamPool.SOURCE_START)) {
					numberOfFoundSources++;
				}
			}
		} catch (FileNotFoundException ex) {
			throw new AssertionFailedError(ex.getMessage());
		} catch (IOException ex) {
			throw new AssertionFailedError(ex.getMessage());
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException ex) {
					throw new AssertionFailedError(ex.getMessage());
				}
		}
	
		assertThat(toBeVarifiedNumberOfSources, is(numberOfFoundSources));
	}
}