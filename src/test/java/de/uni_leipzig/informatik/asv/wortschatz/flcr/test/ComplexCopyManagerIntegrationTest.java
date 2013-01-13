package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.base.Predicate;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.ComplexCopyManager;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.*;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ComplexCopyManagerIntegrationTest extends ComplexCopyManagerUnitTest {

	private static final Logger log = LoggerFactory.getLogger(ComplexCopyManagerIntegrationTest.class);

	@Ignore
	@Test
	public void copyFullFileSet() throws IOException, InterruptedException, ExecutionException {
		this.create();
		
		controller.start();
		
		assertThat(controller.isRunning(), is(true));
		assertThat(controller.isStopped(), is(false));
		
		assertThat(controller.awaitTermination(), is(true));
		
		Thread.sleep(100);
		
		assertThat(controller.isRunning(), is(false));
		assertThat(controller.isStopped(), is(true));
		
		final TextFile textFile = new TextFile(textfileFile);

		final File resultingTextfile = factory.getDefaultTextfileMapping( textFile );
		
		while (!resultingTextfile.exists()) {
			synchronized (this) {
				this.wait(100);
			}
		}
		
		assertThat(resultingTextfile.exists(), is(true));
		TextFile textFileToBeCompared = new TextFile(resultingTextfile);
		
		// waiting until the textFile, which should be compared reaches the maximum number of sources
		synchronized (this) {
			this.wait(100); 
		}
		
		assertSame(resultingTextfile.getAbsolutePath(), textFile, textFileToBeCompared );

	}

	private static void assertSame(final String errorMsg, final TextFile inputOriginal, final TextFile inputChanged)
			throws FileNotFoundException {
		assertThat(inputOriginal, notNullValue());
		assertThat(inputChanged, notNullValue());

		assertThat(inputOriginal.getFile().getAbsolutePath(), 
					inputOriginal.getOutputType(), is(inputChanged.getOutputType()));
		assertThat(errorMsg, inputOriginal.getYear(), is(inputChanged.getYear()));
		assertThat(errorMsg, inputOriginal.getLanguage(), is(inputChanged.getLanguage()));
		assertThat(errorMsg, inputOriginal.getNumberOfSources(), is(inputChanged.getNumberOfSources()));

		/*
		 * the problem with the following line is, that the sources are not comparable (inputOriginal can be called a LIST, while the inputChanged can be called a SET (threads were working on (unsorted list of sources))
		 */
//		
//		try {
//			for (Source source = inputOriginal.getNext(); source != null; source = inputOriginal.getNext()) {
//				assertSame(source, inputChanged.getNext());
//			}
//		} catch (ReachedEndException e) {
//			// everything ok!!!
//		}
		
	}

	private static void assertSame(final Source inputSource, final Source sourceToBeCompared) {
		assertThat(inputSource.toString(), is(sourceToBeCompared.toString()));
		assertThat(inputSource.getLanguage(), is(sourceToBeCompared.getLanguage()));
		assertThat(inputSource.getLocation(), is(sourceToBeCompared.getLocation()));
		assertThat(inputSource.getContent().toString(), is(sourceToBeCompared.getContent().toString()));
	}

	@Ignore
	@Test
	public void splitFullFileSet() throws IOException, InterruptedException, ExecutionException {

		this.create();
		
		// parse the input text file 'textfileFile' for the number of sources, and their domains
		final Hashtable<String, Integer> occurrencesOfDomains = this.parseTextfileForDomains(textfileFile);
		assertThat(occurrencesOfDomains.containsKey("bo"), is(true));
		final int numberOf_SPA_BO_sources = occurrencesOfDomains.get("bo");		
		assertThat(numberOf_SPA_BO_sources, is(1));
		
		// adjust the predicate filter for language 'spa_limited' and domain 'bo'.
		final Predicate<Pair<String,String>> languageSourceDomainFilter = new Predicate<Pair<String,String>>() {

			private static final String LANGUAGE = "spa_limited";
			private static final String DOMAIN = "bo";
			
			private static final String NO_LANGUAGE = "NO_LANGUAGE";
			
			@Override
			public boolean apply(final Pair<String, String> languageDomainPairToBeTested) {
				log.info("Applying this filter to language domain pair: (language: '%s', domain: '%s')", languageDomainPairToBeTested.getFirst(), languageDomainPairToBeTested.getSecond());
				return languageDomainPairToBeTested != null && 
						(
						languageDomainPairToBeTested.getFirst().equalsIgnoreCase(LANGUAGE) ||
						languageDomainPairToBeTested.getFirst().equalsIgnoreCase( NO_LANGUAGE )
						) && 
						languageDomainPairToBeTested.getSecond().equalsIgnoreCase( DOMAIN );
			}
		};
		
		// initialize the instances for copying
		ComplexCopyManager controller = new ComplexCopyManager(textfileFile.getParentFile(), new Configurator());
		// debug testing by injecting a pre-declared language filter: allows only SPA:BO to be split
		controller.getMappingFactory().setLanguageFilter( languageSourceDomainFilter );
		
		// start copying, and wait until the results were written
		controller.start();
		assertThat(controller.awaitTermination(), is(true));
		assertThat(controller.getMappingFactory().getLanguageFilter(), is(languageSourceDomainFilter));
		
		/*
		 * 
		 * how to assert the right number of sources were copied in which order etc.?!
		 * 
		 * 
		 */
		final File outputDirectory = controller.getMappingFactory().getDefaultOutputDirectory( TextFileType.Findlinks);
		assertThat(outputDirectory.exists(), is(true));
		assertThat(outputDirectory.isDirectory(), is(true));
		
		/*
		 * 3 FILES are existing:
		 * 
		 * FL_spa_limited0000.txt
		 * FL____.txt
		 * FL_eng____.txt
		 * 
		 * only two files should be contained in the result set:
		 * 
		 * spa_limited_web-fl
		 * -- spa_limited_web-fl/spa_limited_web-fl_0000.txt
		 * -- spa_limited_web-fl/spa_limited_bo_web-fl_0000.txt
		 * NO_LANGUAGE_web-fl
		 * -- NO_LANGUAGE_web-fl/NO_LANGUAGE_web-fl_0000.txt
		 * -- NO_LANGUAGE_web-fl/NO_LANGUAGE_bo_web-fl_0000.txt
		 */	
		assertThat(outputDirectory.listFiles().length, is(2));
		// NO_LANGUAGE output directory
		// hopefully the files are sorted that way...
		final File noLanguageOutputDirectory = outputDirectory.listFiles()[0];
		
		for (File f : noLanguageOutputDirectory.listFiles()) {
			assertThat(f.getAbsolutePath().contains(outputDirectory.getAbsolutePath()), is(true));
			assertThat(f.getAbsolutePath().contains("NO_LANGUAGE"), is(true));
		}
		
		// spa_limited output directory
		final File defaultOutputDirectory = outputDirectory.listFiles()[1];
		
		assertThat(defaultOutputDirectory.exists(), is(true));
		// default textfile and spa_bo textfile
		assertThat(defaultOutputDirectory.getAbsolutePath(),defaultOutputDirectory.listFiles().length, is(2));
		
		final TextFile defaultOutputTextFile = new TextFile(defaultOutputDirectory.listFiles()[1]);
		final TextFile default_spa_bo_TextFile = new TextFile(defaultOutputDirectory.listFiles()[0]);
		
		// sleep after all the TextFile got calculated
		Thread.sleep(100);
		
		assertThat(defaultOutputDirectory.listFiles()[0].getAbsolutePath(), default_spa_bo_TextFile.getNumberOfSources(), is(numberOf_SPA_BO_sources));
		assertThat(defaultOutputDirectory.listFiles()[1].getAbsolutePath(), defaultOutputTextFile.getNumberOfSources() + numberOf_SPA_BO_sources, is (49));
		
	}

	private String createLanguageDomainFile(String language, Set<String> domainsOfLanguage) throws IOException {
		
		final File languageDomainFile = File.createTempFile("language_domain_file", "_for_testing_only.txt");
		
		FileWriter writer = null;
		
		try {
			writer = new FileWriter(languageDomainFile);
			for (String domainOfLanguage : domainsOfLanguage) {
				writer.write(String.format("%s.%s\n",language, domainOfLanguage));
			}
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		return languageDomainFile.getAbsolutePath();
		
	}

	private Hashtable<String, Integer> parseTextfileForDomains(final File textfileFile) throws IOException {
		
		Hashtable<String,Integer> result = new Hashtable<String,Integer>();
		
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(textfileFile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(SourceInputstreamPool.SOURCE_START)) {
					final String urlString = line.substring(line.indexOf(Source.locationStart)+Source.locationStart.length(), line.indexOf(Source.locationEnd));
					if (urlString != null) {
						final String domain = new Location(new URL(urlString)).getDomain();
						
						if (!result.containsKey(domain)) {
							result.put(domain, 0);
						}
						result.put(domain, result.get(domain)+1);
						
					}
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
		
		
		return result;
	}
	
	
}
