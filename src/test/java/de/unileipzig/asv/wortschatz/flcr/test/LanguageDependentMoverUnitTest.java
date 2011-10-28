/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.unileipzig.asv.wortschatz.flcr.LanguageDependentMover;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class LanguageDependentMoverUnitTest {

	public static final String DIRECTORY = "./tmp/findlinks";
	public static final String SOURCE_DIRECTORY = "./tmp";
	private static final String FILENAME = "FL_deu0001.txt";
	
	private File directory;
	private File file;
	private LanguageDependentMover languageMover;
	private File sourceDirectory;
	
	@Before
	public void setUp() throws IOException {
		directory = new File(DIRECTORY);
		sourceDirectory = new File(SOURCE_DIRECTORY);
		file = new File(sourceDirectory,FILENAME);
		directory.mkdirs();
		file.createNewFile();
		languageMover = new LanguageDependentMover();
		languageMover.setDirectorySuffix("_webfl_2011");
	}
	
	@After
	public void tearDown(){
		sourceDirectory.delete();
	}
	
	@Test
	public void create() {
		
		assertThat(directory.exists(), is(true));
		assertThat(file.exists(), is(true));
		
		File result1 = languageMover.copyFile(file, directory);
		
		assertThat(result1, notNullValue());
		assertThat(result1.exists(), is(true));
		
	}
}
