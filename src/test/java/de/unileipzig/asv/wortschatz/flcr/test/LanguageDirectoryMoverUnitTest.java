/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.unileipzig.asv.wortschatz.flcr.LanguageDirectoryMover;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class LanguageDirectoryMoverUnitTest {

	private File stopwort;
	private File unigramm;
	private File trigramm;
	
	private LanguageDirectoryMover webcrawl;
	
	private Integer COUNT = 0;
	
	@Before
	public void setUp() throws IOException {
		
		File directory = new File("./tmp/webcrawl");
		directory.mkdirs();
		
		webcrawl = new LanguageDirectoryMover();
		webcrawl.setDirectorySuffix("_web-cr_2011");
		webcrawl.setOutputDirectory(new File("./tmp/tgrigull/flcr/"));
		
		stopwort = new File(directory,"Stopwort");
		stopwort.mkdirs();
		this.fillDirectory(stopwort);
		unigramm = new File(directory,"Unigramm");
		unigramm.mkdirs();
		this.fillDirectory(unigramm);
		trigramm = new File(directory,"Trigramm");
		trigramm.mkdirs();
		this.fillDirectory(trigramm);
		
		webcrawl.addInputDirectory(stopwort);
		webcrawl.addInputDirectory(unigramm);
		webcrawl.addInputDirectory(trigramm);		
	}
	
	/**
	 * @param file
	 * @throws IOException 
	 */
	private void fillDirectory(File directory) throws IOException {
		
		File file1 = new File(directory,"CR_spa"+new DecimalFormat("0000").format(COUNT++)+".txt");
		file1.createNewFile();
		File file2 = new File(directory,"CR_spa-int"+new DecimalFormat("0000").format(COUNT++)+".txt");
		file2.createNewFile();
		File file3 = new File(directory,"CR_spa"+new DecimalFormat("0000").format(COUNT++)+".txt");
		file3.createNewFile();
		File file4 = new File(directory,"CR_spa"+new DecimalFormat("0000").format(COUNT++)+".txt");
		file4.createNewFile();
		File file5 = new File(directory,"CR_spa"+(COUNT++)+".txt");
		file5.createNewFile();
		
	}

	@Test
	public void create() throws IOException {
		
		webcrawl.searchDirectories();
		
	}
	
	@Test
	public void bzipOutputFiles() throws IOException {
		
		Properties properties = new Properties();
		properties.setProperty("file.output.bzip", "true");
		webcrawl.loadProperties(properties);
		
		webcrawl.searchDirectories();
		
		for (File languageDirectory : webcrawl.getOutputDirectory().listFiles()) {
			for (File file : languageDirectory.listFiles()) {
				assertThat(file.getName().endsWith(".txt.bz2"), is(true));
			}
		}
	}

	
}
