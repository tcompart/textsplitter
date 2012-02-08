/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.unileipzig.asv.wortschatz.flcr.TextFile;
import de.unileipzig.asv.wortschatz.flcr.util.IOUtil;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class TextFileUnitTest {

	private TextFile textFile;
	
	private File testDirectory;
	
	@Before
	public void setUp() throws IOException {
		
		testDirectory = new File("./tmp/textfile");
		if (!testDirectory.exists()) {
			testDirectory.mkdirs();
		}
		
		textFile = TextFile.newTextFile(testDirectory, "testFile", 0, null);
	}
	
	@After
	public void tearDown() {
		IOUtil.removeDirectory(testDirectory);
	}
	
	@Test
	public void create() {
		
		assertThat(textFile, notNullValue());
		assertThat(textFile.getFileName(), is("testFile"));
		assertThat(textFile.getFileCount(), is(0));
		assertThat(textFile.getFilesuffix(), is(".txt"));
		assertThat(textFile.getSize(), is((long) 0));
		assertThat(textFile.getFileHashSourceFile(), nullValue());
		assertThat(textFile.getFile(), notNullValue());
		assertThat(textFile.getFile().exists(), is(true));
	}
	
	@Test
	public void next() throws IOException {
		
		TextFile newTextFile = TextFile.next(textFile);
		
		assertThat(newTextFile, notNullValue());
		assertThat(newTextFile.getFileName(), is("testFile"));
		assertThat(newTextFile.getFileCount(), is(1));
		assertThat(newTextFile.getFilesuffix(), is(".txt"));
		assertThat(newTextFile.getSize(), is((long) 0));
		assertThat(newTextFile.getFileHashSourceFile(), nullValue());
		assertThat(newTextFile.getFile(), notNullValue());
		assertThat(newTextFile.getFile().exists(), is(true));
		
		
	}
	
}
