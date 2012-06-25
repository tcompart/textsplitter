/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.EventListener;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.EventListener.Event;
import de.unileipzig.asv.wortschatz.flcr.WortschatzResourceStart;
import de.unileipzig.asv.wortschatz.flcr.exception.ExitException;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class WortschatzResourceStartUnitTest {

	private static class NoExitSecurityManager extends SecurityManager 
    {
        @Override
        public void checkPermission(Permission perm) 
        {
                // allow anything.
        }
        @Override
        public void checkPermission(Permission perm, Object context) 
        {
                // allow anything.
        }
        @Override
        public void checkExit(int status) 
        {
                super.checkExit(status);
                throw new ExitException(status);
        }
    }

	@Before
	public void setUp() {
		System.setSecurityManager(new NoExitSecurityManager());
	}
	
	@After
	public void tearDown() {
		System.setSecurityManager(null);
		IOUtil.removeDirectory(new File("./tmp"));
	}
	
	@Test
	public void testPrintScreen() throws IOException {
		
		BufferedReader reader = new BufferedReader(new StringReader(WortschatzResourceStart.printHelp()));
		assertThat(reader.readLine(), is("Supported Parameters:"));
		assertThat(reader.readLine(), is("\t--help\t\t:\tthis help screen."));
		assertThat(reader.readLine(), is("\t--findlinks\t:\ta list of directories, which should be used as input for the findlinks data, or a single value as a main directory for directories like 'Stopwort','Unigramm' and 'Trigramm'."));
		assertThat(reader.readLine(), is("\t--webcrawl\t:\ta list of directories, which should be used as input for the web crawl data, or a single value as a main directory for directories like 'Stopwort','Unigramm' and 'Trigramm'."));
		assertThat(reader.readLine(), is("\t--output\t:\ta file path were the output directory should be created."));
		assertThat(reader.readLine(), is("\t--bzip\t:\tthis option enables the flag for compressing written files. The currently used compress algorithm is provided by bzip2."));
		assertThat(reader.readLine(), is("\t--filesize\t:\ta value assigned as a value (Long). The value is in MegaByte. Therefore one GigaByte would be: '--filesize 1024'."));
		assertThat(reader.readLine(), is("\t--properties\t:\tpath to a property file; default: properties.prop. Those are loaded before the start. However highest priority have directly called parameters and their values. See information below of the possible properties and their default values."));
		assertThat(reader.readLine(), is("\t--dryrun\t:\tfor testing purpose. This creates instances but the copy process is not started. This enables the check of parameters and properties."));
		assertThat(reader.readLine(), is(""));
		assertThat(reader.readLine(), is("Supported Properties:"));
		assertThat(reader.readLine(), is("\t"+WortschatzResourceStart.propertyOutputDirectoryName+": '"+WortschatzResourceStart.propertyOutputDirectoryDefaultValue+"'"));
		assertThat(reader.readLine(), is("\t"+WortschatzResourceStart.propertyWebCrawlDirectorySuffix+": '"+WortschatzResourceStart.propertyWebCrawlDirectorySuffixDefaultValue+"'"));
		assertThat(reader.readLine(), is("\t"+WortschatzResourceStart.propertyFindlinksDirectorySuffix+": '"+WortschatzResourceStart.propertyFindlinksDirectorySuffixDefaultValue+"'"));
		assertThat(reader.readLine(), is(""));
		
	}
	
	@Test
	public void testInfoScreen() throws IOException {
		
		BufferedReader reader = new BufferedReader(new StringReader(WortschatzResourceStart.printIntro()));
		assertThat(reader.readLine(), is("Program: ----- "+WortschatzResourceStart.PROGRAM_NAME+" -----"));
		assertThat(reader.readLine(), is("Version: "+WortschatzResourceStart.VERSION));
		assertThat(reader.readLine(), is(""));
		
	}
	
	@Test
	public void testCallHelp() throws IOException {
		String[] arguments = new String[]{"--help"};
		try {
			WortschatzResourceStart.main(arguments);
		} catch (ExitException ex) {
			assertThat(ex.status, is(0));
		}
		
	}
	
	@Test(expected=RuntimeException.class)
	public void testMissingOption() throws IOException {
		String[] arguments = new String[]{};
		WortschatzResourceStart.main(arguments);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void testMissingInputDirectory() throws IOException {
		File temporaryDirectory = new File("./tmp");
		if (!temporaryDirectory.exists()) {
			temporaryDirectory.mkdir();
		}
		assertThat(temporaryDirectory.exists(), is(true));
		assertThat(new File("./inputDirectory").exists(), is(false));
		String[] arguments = new String[] {
				"--findlinks",
				"./inputDirectory",
				"--webcrawl",
				"./tmp"
		};
		WortschatzResourceStart.main(arguments);
	}
	
	@Test
	public void wsrIntegrationTest() throws IOException {
		
		File source = new File("./tmp/source");
		File sourceFindlinks = new File(source,"findlinks");
		File findlinksStopwort = new File(sourceFindlinks, "Stopwort");
		findlinksStopwort.mkdirs();
		File findlinksUnigramm = new File(sourceFindlinks, "Unigramm");
		findlinksUnigramm.mkdirs();
		File findlinksTrigramm = new File(sourceFindlinks, "Trigramm");
		findlinksTrigramm.mkdirs();
		
		File sourceWebCrawl = new File(source,"webcrawl");
		File webcrawlStopwort = new File(sourceWebCrawl, "Stopwort");
		webcrawlStopwort.mkdirs();
		File webcrawlUnigramm = new File(sourceWebCrawl, "Unigramm");
		webcrawlUnigramm.mkdirs();
		File webcrawlTrigramm = new File(sourceWebCrawl, "Trigramm");
		webcrawlTrigramm.mkdirs();
		
		this.fillDirectory(findlinksStopwort, "FL_");
		this.fillDirectory(findlinksUnigramm, "FL_");
		this.fillDirectory(findlinksTrigramm, "FL_");
		this.fillDirectory(webcrawlStopwort, "CR_");
		this.fillDirectory(webcrawlUnigramm, "CR_");
		this.fillDirectory(webcrawlTrigramm, "CR_");
		
		File output = new File("./tmp/output/content/");
		
		assertThat(output.exists(), is(false));
		
		String[] arguments = new String[] {
				"--output",
				output.getCanonicalPath(),
				"--findlinks",
				sourceFindlinks.getCanonicalPath(),
				"--webcrawl",
				sourceWebCrawl.getCanonicalPath(),
		};
		
		assertThat(output.exists(), is(false));
		
		WortschatzResourceStart.main(arguments);
		
		assertThat(output.exists(), is(true));
		assertThat(output.listFiles()[0].getName(), is("deu_ch_web-cr_2011"));
		assertThat(output.listFiles()[1].getName(), is("deu_ch_web-fl_2011"));
		assertThat(output.listFiles()[2].getName(), is("deu_web-cr_2011"));
		assertThat(output.listFiles()[3].getName(), is("deu_web-fl_2011"));
	}

	/**
	 * @param findlinksStopwort
	 * @throws IOException 
	 */
	private void fillDirectory(File directory, String prefix) throws IOException {
		File f1 = new File(directory,prefix+"deu0000.txt");
		f1.createNewFile();
		File f2 = new File(directory,prefix+"deu0001.txt");
		f2.createNewFile();
		File f3 = new File(directory,prefix+"deu0002.txt");
		f3.createNewFile();
		File f4 = new File(directory,prefix+"deu0003.txt");
		f4.createNewFile();
		File f5 = new File(directory,prefix+"deu-eng0000.txt");
		f5.createNewFile();
		File f6 = new File(directory,prefix+"deu_ch0000.txt");
		f6.createNewFile();
		File f7 = new File(directory,prefix+"___-deu0000.txt");
		f7.createNewFile();
		File f8 = new File(directory,prefix+"d___eu0000.txt");
		f8.createNewFile();
	}
	
}
