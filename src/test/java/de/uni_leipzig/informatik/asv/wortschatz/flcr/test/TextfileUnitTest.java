package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
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

		Resource textfileResource2 = new FileSystemResource("Unigramm/FL_spa0000.txt");
		assertThat(textfileResource2.exists(), is(true));

		file2 = textfileResource2.getFile();
	}


	private boolean disableYear;
	
	@Test
	public void parseTextfileNames() {
		assertThat(Textfile.searchYearInFileName(Textfile.inputFileNamePattern, "FL_spa0000.txt", 3).isNothing(), is(true));
		assertThat(Textfile.searchYearInFileName(Textfile.inputFileNamePattern, "FL_2011_spa0000.txt", 3).isJust(), is(true));
		
		assertThat(Textfile.searchLanguageInFileName(Textfile.inputFileNamePattern, "FL_spa0000.txt", 4).isNothing(), is(false));
		assertThat(Textfile.searchLanguageInFileName(Textfile.inputFileNamePattern, "FL_2011_spa0000.txt", 4).isJust(), is(true));
		
		assertThat(Textfile.searchYearInFileName(Textfile.outputFileNamePattern, "spa_limited_web-fl_2011.txt", 5).isJust(), is(true));
		assertThat(Textfile.searchYearInFileName(Textfile.outputFileNamePattern, "spa_limited_web-fl_2011.txt", 5).getValue(), is(2011));
		assertThat(Textfile.searchYearInFileName(Textfile.outputFileNamePattern, "spa_limited_web-fl_2011_0000.txt", 5).isJust(), is(true));
		assertThat(Textfile.searchYearInFileName(Textfile.outputFileNamePattern, "spa_limited_web-fl_2011_0000.txt", 5).getValue(), is(2011));
		
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_web-fl_2011.txt", 1).isJust(), is(true));
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_web-fl_2011.txt", 1).getValue(), is("spa"));
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_limited_web-fl_2011.txt", 1).getValue(), is("spa_limited"));
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_limited_web-fl_2011_0000.txt", 1).isJust(), is(true));
		assertThat(Textfile.searchLanguageInFileName(Textfile.outputFileNamePattern, "spa_limited_web-fl_2011_0000.txt", 1).getValue(), is("spa_limited"));
		
	}
	
	
	private static final String DIVISION_SIGN = "_";

	@Test
	public void splitFilesByName() {
		
		this.disableYear = false;
		
		assertThat("spa_web-fl_2012_0000.txt", is(newString("spa_web-fl_2012.txt")));
		assertThat("spa_web-fl_2012_0001.txt", is(newString("spa_web-fl_2012_0000.txt")));
		assertThat("spa_web-fl_2012_0002.txt", is(newString("spa_web-fl_2012_0001.txt")));
		assertThat("spa_web-fl_2012_0003.txt", is(newString("spa_web-fl_2012_0002.txt")));
		assertThat("spa_web-fl_2012_0004.txt", is(newString("spa_web-fl_2012_0003.txt")));
		
		this.disableYear = true;
		
		assertThat("spa_web-fl_2013.txt", is(newString("spa_web-fl_2012.txt")));
		assertThat("spa_web-fl_2014.txt", is(newString("spa_web-fl_2013.txt")));
		assertThat("spa_web-fl_2015.txt", is(newString("spa_web-fl_2014.txt")));
		assertThat("spa_web-fl_2016.txt", is(newString("spa_web-fl_2015.txt")));
		
		this.disableYear = false;

		assertThat("spa_web-fl_2016.txt", is(newString("spa_web-fl_2015.txt")));
	}
	
	public String newString(final String previousString) {
		final String fileName = previousString;
		
		final Pattern fileNamePrefixPattern = Pattern.compile("(([a-z_-]+)(\\d{4})?(_(\\d{4}))?)");
		
		Matcher matcher = fileNamePrefixPattern.matcher(fileName);
		final String fileNamePrefix;
		final String theMiddle;
		final String fileNameSuffix = ".txt";
		if (matcher.find()) {
			fileNamePrefix = matcher.group(2);
			
			if (matcher.group(3) == null || matcher.group(3).isEmpty()) {
				theMiddle = String.format("%04d", 0);
			} else if (matcher.group(3).equals(this.getYear())) {
					if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
						theMiddle = String.format("%s%s%04d", matcher.group(3), DIVISION_SIGN, (Integer.parseInt(matcher.group(5))+1));
					} else {
						theMiddle = String.format("%s%s%04d", matcher.group(3), DIVISION_SIGN, 0);
					}
			} else {
				theMiddle = String.format("%04d", (Integer.parseInt(matcher.group(3))+1));
			}
		} else {
			return null;
		}
	
		return fileNamePrefix + theMiddle + fileNameSuffix;
	}
	
	
	private String getYear() {
		if (disableYear) {
			return "";
		}
		return "2012";
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
