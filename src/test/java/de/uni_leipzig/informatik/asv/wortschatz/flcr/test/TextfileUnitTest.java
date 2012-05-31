package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextfileType;

public class TextfileUnitTest {

	private static final Logger log = LoggerFactory.getLogger(TextfileUnitTest.class);

	protected static File file1;
	protected static File file2;

	@BeforeClass
	public static void setUpClass() throws IOException {
		Resource textfileResource1 = new ClassPathResource("Unigramm/FL_spa_limited0000.txt");
		assertThat(textfileResource1.exists(), is(true));

		file1 = textfileResource1.getFile();

		Resource textfileResource2 = new ClassPathResource("Unigramm/FL_spa0000.txt");
		assertThat(textfileResource2.exists(), is(true));

		file2 = textfileResource2.getFile();
	}
	
	@Test
	public void parseTextfileNames() {
		assertThat(Textfile.searchYearInFileName(Textfile.inputFileNamePattern, "FL_spa0000.txt", 3).isNothing(), is(true));
		assertThat(Textfile.searchYearInFileName(Textfile.inputFileNamePattern, "FL_2011_spa0000.txt", 3).isJust(), is(true));
		
		assertThat(Textfile.searchLanguageInFileName(Textfile.inputFileNamePattern, "FL_spa0000.txt", 4).isNothing(), is(false));
		assertThat(Textfile.searchLanguageInFileName(Textfile.inputFileNamePattern, "FL_2011_spa0000.txt", 4).isJust(), is(true));
		
		assertThat(Textfile.searchYearInFileName(Textfile.outputFileNamePattern, "spa_limited_webfl_2011.txt", 5).isJust(), is(true));
		assertThat(Textfile.searchYearInFileName(Textfile.outputFileNamePattern, "spa_limited_webfl_2011.txt", 5).getValue(), is(2011));
		assertThat(Textfile.searchYearInFileName(Textfile.outputFileNamePattern, "spa_limited_webfl_2011_0000.txt", 5).isJust(), is(true));
		assertThat(Textfile.searchYearInFileName(Textfile.outputFileNamePattern, "spa_limited_webfl_2011_0000.txt", 5).getValue(), is(2011));
		
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_webfl_2011.txt", 1).isJust(), is(true));
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_webfl_2011.txt", 1).getValue(), is("spa"));
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_limited_webfl_2011.txt", 1).getValue(), is("spa_limited"));
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_limited_webfl_2011_0000.txt", 1).isJust(), is(true));
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_limited_webfl_2011_0000.txt", 1).getValue(), is("spa_limited"));
		
	}
	
	@Test(timeout = 200)
	public void createSmall() throws IOException {
		Textfile textfile1 = new Textfile(file1);

		assertThat(textfile1.getFile(), is(file1));
		assertThat(textfile1.getLanguage(), is("spa_limited"));
		assertThat(textfile1.getYear(), is(Textfile.DEFAULT_YEAR));
		assertThat(textfile1.getTextfileName(), is(file1.getName()));
		assertThat(textfile1.getOutputType(), is(TextfileType.Findlinks));
//		assertThat(textfile1.getNumberOfSources() > 0, is(true));
	}

	@Test(timeout = 200)
	public void createBigger() throws IOException {
		Textfile textfile2 = new Textfile(file2);

		assertThat(textfile2.getFile(), is(file2));
		assertThat(textfile2.getLanguage(), is("spa"));
		assertThat(textfile2.getYear(), is(Textfile.DEFAULT_YEAR));
		assertThat(textfile2.getTextfileName(), is(file2.getName()));
		assertThat(textfile2.getOutputType(), is(TextfileType.Findlinks));
		/*
		 * the problem is the number of sources.
		 * The sources are parsed asynchronous in the back during text file
		 * creation or source pool creation in detail.
		 * The total number of sources is known after a while of parsing (source
		 * name, domain, number of lines etc.)
		 */
		// assertThat(textfile2.getNumberOfSources() > 0, is(true));
		//
	}

}
