package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

import de.uni_leipzig.asv.clarin.common.tuple.Pair;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.CopyController;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Location;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.SourceInputstreamPool;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextfileType;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.TextfileLanguageFilter;

public class CopyControllerIntegrationTest extends CopyControllerUnitTest {

	private static final Logger log = LoggerFactory.getLogger(CopyControllerIntegrationTest.class);
	
	@Test
	public void copyFullFileSet() throws IOException, InterruptedException {
		this.create();

		Collection<File> fileSet = new HashSet<File>();
		fileSet.add(textfileFile);
		controller.start(fileSet);
		
		Thread.sleep(100);
		
		assertThat(controller.isRunning(), is(true));
		assertThat(controller.isStoped(), is(false));
		
		synchronized (this) {
			while (controller.isRunning())
				this.wait(3000);
		}
		
		assertThat(controller.isRunning(), is(false));
		assertThat(controller.isStoped(), is(true));

		synchronized (this) {
			this.wait(3000);
		}
		
		final Textfile textfile = new Textfile(textfileFile);

		final File resultingTextfile = factory.getDefaultTextfileMapping(textfile);
		
		while (!resultingTextfile.exists()) {
			synchronized (this) {
				wait(100);
			}
		}
		
		assertThat(resultingTextfile.exists(), is(true));
		
		assertSame(textfile, new Textfile(resultingTextfile));

	}

	private static void assertSame(final Textfile inputOriginal, final Textfile inputChanged)
			throws FileNotFoundException {
		assertThat(inputOriginal, notNullValue());
		assertThat(inputChanged, notNullValue());

		assertThat(inputOriginal.getFile().getAbsolutePath(), 
					inputOriginal.getOutputType(), is(inputChanged.getOutputType()));
		assertThat(inputOriginal.getYear(), is(inputChanged.getYear()));
		assertThat(inputOriginal.getLanguage(), is(inputChanged.getLanguage()));
		assertThat(inputOriginal.getNumberOfSources(), is(inputChanged.getNumberOfSources()));

		try {
			for (Source source = inputOriginal.getNext(); source != null; source = inputOriginal.getNext()) {
				assertSame(source, inputChanged.getNext());
			}
		} catch (ReachedEndException e) {
			// everything ok!!!
		}
		
	}

	private static void assertSame(final Source inputSource, final Source sourceToBeCompared) {
		assertThat(inputSource.toString(), is(sourceToBeCompared.toString()));
		assertThat(inputSource.getLanguage(), is(sourceToBeCompared.getLanguage()));
		assertThat(inputSource.getLocation(), is(sourceToBeCompared.getLocation()));
		assertThat(inputSource.getContent().toString(), is(sourceToBeCompared.getContent().toString()));
	}
	
	@Test
	public void splittFullFileSet() throws IOException, InterruptedException {

		this.create();

		Collection<File> fileSet = new HashSet<File>();
		fileSet.add(textfileFile);
		
		// parse the input text file 'textfileFile' for the number of sources, and their domains
		final Hashtable<String, Integer> occurrencesOfDomains = this.parseTextfileForDomains(textfileFile);
		assertThat(occurrencesOfDomains.containsKey("bo"), is(true));
		final int numberOf_SPA_BO_sources = occurrencesOfDomains.get("bo");		
		assertThat(numberOf_SPA_BO_sources, is(1));
		
		// adjust the predicate filter for language 'spa_limited' and domain 'bo'.
		final Predicate<Pair<String,String>> languageSourceDomainFilter = new Predicate<Pair<String,String>>() {

			private static final String LANGUAGE = "spa_limited";
			private static final String DOMAIN = "bo";
			
			@Override
			public boolean apply(final Pair<String, String> languageDomainPairToBeTested) {
				log.info(String.format("Applying this filter to language domain pair: (language: '%s', domain: '%s')",languageDomainPairToBeTested.first(), languageDomainPairToBeTested.second()));
				return languageDomainPairToBeTested != null && languageDomainPairToBeTested.first().equalsIgnoreCase(LANGUAGE) && languageDomainPairToBeTested.second().equalsIgnoreCase(DOMAIN);
			}
		};
		
		// initialize the instances for copying
		CopyController controller = new CopyController();
		// debug testing by injecting a pre-declared language filter: allows only SPA:BO to be split
		controller.getMappingFactory().setLanuageFilter(languageSourceDomainFilter);
		
		// start copying, and wait until the results were written
		controller.start(fileSet);
		synchronized (this) {
			while (controller.isRunning())
				this.wait(3000);
		}
		
		assertThat(controller.getMappingFactory().getLanguageFilter(), is(languageSourceDomainFilter));
		
		/*
		 * 
		 * how to assert the right number of sources were copied in which order etc.?!
		 * 
		 * 
		 */
		final File outputDirectory = controller.getMappingFactory().getDefaultOutputDirectory(TextfileType.Findlinks);
		assertThat(outputDirectory.exists(), is(true));
		assertThat(outputDirectory.isDirectory(), is(true));
		assertThat(outputDirectory.listFiles().length, is(1));
		final File defaultOutputDirectory = outputDirectory.listFiles()[0];
		
		assertThat(defaultOutputDirectory.exists(), is(true));
		// default textfile and spa_bo textfile
		assertThat(defaultOutputDirectory.getAbsolutePath(),defaultOutputDirectory.listFiles().length, is(2));
		
		final Textfile defaultOutputTextfile = new Textfile(defaultOutputDirectory.listFiles()[1]);
		final Textfile default_spa_bo_Textfile = new Textfile(defaultOutputDirectory.listFiles()[0]);
		
		assertThat(defaultOutputDirectory.listFiles()[0].getAbsolutePath(),default_spa_bo_Textfile.getNumberOfSources(), is(numberOf_SPA_BO_sources));
		assertThat(defaultOutputDirectory.listFiles()[1].getAbsolutePath(),defaultOutputTextfile.getNumberOfSources() + numberOf_SPA_BO_sources, is (49));
		
//		// this call is actually pretty bad, because a second instance of class Textfile has to be created... just for a simple file access
//		final Textfile textfile = new Textfile(textfileFile);
//
//		final File defaultOutputFile = factory.getDefaultTextfileMapping(textfile);
//		
//		while (!defaultOutputFile.exists()) {
//			synchronized (this) {
//				wait(100);
//			}
//		}
//		
//		// xxxx_YEAR_NUMBER.txt (deu_webfl(_2012(_0000).txt)
//		final Pattern pattern = Pattern.compile("web(fl|cr)_(_\\d{4}(_\\d{4})?)?\\.txt$");
//		
//		final String defaultOutputFilePath = defaultOutputFile.getAbsolutePath();
//		
//		int totalNumberOfOutsourcedSources = 0;
//		for (Map.Entry<String, Integer> entry : occurrencesOfDomains.entrySet()) {
//			final String domain = entry.getKey();
//			final int numberOfSources = entry.getValue();
//			
//			Matcher matcher = pattern.matcher(defaultOutputFilePath);
//			StringBuffer sb = new StringBuffer();
//			if (matcher.find()) {
//				matcher.appendReplacement(sb, "_" + domain + "_" + textfile.getOutputType().getOutputName()+ "_" +factory.getYear(textfile)+".txt");
//			}
//			matcher.appendTail(sb);
//			
//			assertThat(sb.toString(), sb.toString(), nullValue());
//			
//			final File outputFileForSource = new File(sb.toString());
//			
//			totalNumberOfOutsourcedSources+=numberOfSources;
//		}
//		
//		assertThat(defaultOutputFile.exists(), is(true));
		
//		assertFileConsistsOf(defaultOutputFile, textfile.getNumberOfSources()-totalNumberOfOutsourcedSources);
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
