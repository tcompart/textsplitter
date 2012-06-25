/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import de.unileipzig.asv.wortschatz.flcr.LanguageDependentMover;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class LanguageDependentMoverUnitTest {

	public static final String DIRECTORY = "./tmp/findlinks";
	public static final String SOURCE_DIRECTORY = "./tmp";
	private static final String FILENAME1 = "FL_deu0001.txt";
	private static final String FILENAME2 = "FL_008.txt";
	private static final String FILENAME3 = "CR_deu0000.txt";
	private static final String FILENAME4 = "CR____-deu0000.txt";
	private static final String FILENAME5 = "CR_eng-deu0000.txt";
	
	private static File directory;
	private static File file1;
	private static File file2;
	private static File file3;
	private static File file4;
	private static File file5;
	private static LanguageDependentMover languageMover;
	private static File sourceDirectory;
	
	@BeforeClass
	public static void setUp() throws IOException {
		directory = new File(DIRECTORY);
		
		directory.mkdirs();
		
		sourceDirectory = new File(SOURCE_DIRECTORY);
		
		file1 = createExampleFile(sourceDirectory, FILENAME1);
		file2 = createExampleFile(sourceDirectory, FILENAME2);
		file3 = createExampleFile(sourceDirectory, FILENAME3);
		file4 = createExampleFile(sourceDirectory, FILENAME4);
		file5 = createExampleFile(sourceDirectory, FILENAME5);
		
		languageMover = new LanguageDependentMover();
		languageMover.setDirectorySuffix("_web-fl_2011");
	}
	
	/**
	 * @param directory
	 * @param filename
	 * @throws IOException 
	 */
	private static File createExampleFile(File directory, String filename) throws IOException {
		File file = new File(sourceDirectory,filename);
		directory.mkdirs();
		file.createNewFile();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(writeALongSentence());
		writer.flush();
		writer.close();
		return file;
	}

	/**
	 * @return
	 */
	private static String writeALongSentence() {
		return "A very very long time ago. There was a prince."+
	
		""
		;
	}

	@AfterClass
	public static void tearDown(){
		IOUtil.removeDirectory(directory);
		IOUtil.removeDirectory(sourceDirectory);
	}
	
	@Test
	public void create() {
		
		assertThat(directory.exists(), is(true));
		assertThat(sourceDirectory.exists(), is(true));
		assertThat(file1.exists(), is(true));
		assertThat(file2.exists(), is(true));
		assertThat(file3.exists(), is(true));
		assertThat(file4.exists(), is(true));
		assertThat(file5.exists(), is(true));
	}
	
	@Test
	public void copyFile1() throws IOException {
		File result1 = languageMover.copyFile(file1, directory);
		
		assertThat(directory.listFiles()[0].listFiles().length, is(1));
		
		assertThat(result1, notNullValue());
		assertThat(result1.exists(), is(true));
		assertThat(result1.getName(), is("deu_web-fl_2011_0000.txt"));
	}
	
	@Test
	public void copyFile2() throws IOException {
		File result1 = languageMover.copyFile(file2, directory);
		
		assertThat(result1, nullValue());
	}
	
	
	@Test
	public void copyFile3() throws IOException {
		
		File result1 = languageMover.copyFile(file3, directory);
		
		assertThat(directory.listFiles()[0].listFiles().length, is(2));
		
		assertThat(result1, notNullValue());
		assertThat(result1.exists(), is(true));
		assertThat(result1.getName(), is("deu_web-fl_2011_0001.txt"));
	}

	
	@Test
	public void copyFile4() throws IOException {
		File result1 = languageMover.copyFile(file4, directory);
		
		assertThat(result1, nullValue());
	}
	
	
	@Test
	public void copyFile5() throws IOException {
		File result1 = languageMover.copyFile(file5, directory);
		
		assertThat(result1, nullValue());
	}
	
	@Test
	public void splitCopiedFile() throws IOException {
		
		File inputFile = this.prepareInputFile("CR_deu0002.txt");
		
		assertThat(inputFile.exists(),is(true));
		
		Properties properties = new Properties();
		/*
		 * the input size max is actually just a value for checking of size.
		 * the value has to be just below the next <source><location> opening tag
		 * the current input file preparation would allow also a value to 400 bytes.
		 */
		properties.setProperty("file.input.size.max", "1");
		
		languageMover.loadProperties(properties);
		
		/*
		 * The last created output file is returned by copyFile(file,directory)
		 */
		File outputFile = languageMover.copyFile(inputFile, directory);
		
		assertThat(outputFile, notNullValue());
		assertThat(outputFile.exists(), is(true));
		assertThat(outputFile.getName(), is("deu_web-fl_2011_0003.txt"));
		this.assertContent(outputFile,"<source><location>http://www.operundtanz.de/archiv/2010/04/vdo-nach.shtml</location><date>2011-00-04</date><user>Wiederitzsch</user><original_encoding>iso-8859-1</original_encoding><language>deu</language></source>");
		
		File nextFile = new File(new File(directory,"deu_web-fl_2011"),"deu_web-fl_2011_0002.txt");
		
		assertThat(nextFile, notNullValue());
		assertThat(nextFile.exists(), is(true));
		assertThat(nextFile.getName(), is("deu_web-fl_2011_0002.txt"));
		this.assertContent(nextFile,"<source><location>http://www.oekobuero.de/fgbpro13.htm</location><date>2011-00-05</date><user>WIederitzsch</user><original_encoding>iso-8859-1</original_encoding><language>deu</language></source>");
		
	}

	/**
	 * @param outputFile
	 * @param array
	 * @throws IOException 
	 */
	private void assertContent(File outputFile, String startLine) throws IOException {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile)));
		
		String line = in.readLine();
		in.close();
		
		assertThat(startLine.isEmpty(), is(false));
		assertThat(line.isEmpty(), is(false));
		assertThat(line, is(startLine));
	}

	/**
	 * @param string
	 * @return
	 * @throws IOException 
	 */
	private File prepareInputFile(String string) throws IOException {
		File file = new File(sourceDirectory,string);
		
		if (file.exists()) {
			file.delete();
		}
		
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		out.write("<source><location>http://www.oekobuero.de/fgbpro13.htm</location><date>2011-00-05</date><user>WIederitzsch</user><original_encoding>iso-8859-1</original_encoding><language>deu</language></source>");out.newLine();
		out.newLine();
		out.newLine();
		out.write("Protokoll der 13. Sitzung");out.newLine();
		out.newLine();
		out.write("des Fahrgastbeirates Raum Hanau");out.newLine();
		out.write("vom 08. November 2000");out.newLine();
		out.write("Protokollführer: Björn Vellguth");out.newLine();
		out.newLine();
		out.write("<source><location>http://www.operundtanz.de/archiv/2010/04/vdo-nach.shtml</location><date>2011-00-04</date><user>Wiederitzsch</user><original_encoding>iso-8859-1</original_encoding><language>deu</language></source>");out.newLine();
		out.newLine();
		out.write("Zur Situation deutscher Theater und Orchester");out.newLine();
		out.newLine();
		out.write("Zwischen");out.newLine();
		out.write("Hoffen und Bangen");out.newLine();
		out.newLine();
		out.write("Die Situation des Sorbischen National-Ensembles (SNE)");out.newLine();
		out.newLine();
		out.write("Sorbische Traditionen"); out.newLine();
		out.write("bewahren");out.newLine();
		out.newLine();
		out.write("Ein Gespräch mit der");out.newLine();
		out.write("Intendantin des SNE, Milena Vettraino");out.newLine();
		out.flush();
		out.close();
		
		assertThat(file.length() > 700, is(true));
		
		return file;
	}
	
	
}
