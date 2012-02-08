/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.unileipzig.asv.wortschatz.flcr.util.Checksum;
import de.unileipzig.asv.wortschatz.flcr.util.IOUtil;
import de.unileipzig.asv.wortschatz.flcr.util.StringLanguageParser;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class LanguageDependentMover {
	
	private static final String BZIP_EXECUTABLE = "/usr/bin/bzip2";

	private Long FILE_SIZE_MAX;

	private Boolean BZIP_FILE;

	protected StringLanguageParser languageParser;

	protected String directory_suffix = "";
	
	public void loadProperties(Properties properties) {
		FILE_SIZE_MAX = Long.parseLong(properties.getProperty("file.input.size.max", "0"));
		BZIP_FILE = Boolean.parseBoolean(properties.getProperty("file.output.bzip"));
	}
	
	public LanguageDependentMover() {		
		languageParser = new StringLanguageParser();
		languageParser.setPrefix("FL_");
		languageParser.setSuffix("0000.txt");		
		
		this.loadProperties(new Properties());
		
	}
	
	public StringLanguageParser getLanguageParser() {
		return this.languageParser;
	}
	
	public void setLanguageParser(StringLanguageParser parser) {
		if (parser == null) {
			throw new IllegalArgumentException("The parser has to be initialized for a succesful run.");
		}
		languageParser = parser;
	}
	
	public void setDirectorySuffix(String directorySuffix) {
		this.directory_suffix = (directorySuffix != null ? directorySuffix : "");
	}
	
	public File copyFile(File file, File directory) throws IOException {
		
		String language = this.languageParser.updateName(file.getName());
		
		if (this.checkLanguage(language)) {
			File languageDirectory = this.createNewLanguageDirectory(directory,language+directory_suffix);
			
			String checkSum = createChecksum(file);
			
			return this.copy(file, this.createNewTextFile(languageDirectory, language+directory_suffix, 0, checkSum)).getFile();
		}
		
		return null;
	}

	/**
	 * @param file
	 * @return
	 */
	public static String createChecksum(File file) {
		String checkSum = null;
		try {
			checkSum = Checksum.getChecksumSHA1(file);
		} catch (IOException e) {
			// TODO fehlerbehandlung...
			e.printStackTrace();
		}
		return checkSum;
	}

	/**
	 * @param parentDirectory
	 * @param directoryName
	 * @return
	 */
	private File createNewLanguageDirectory(File parentDirectory, String directoryName) {
		File languageDirectory = new File(parentDirectory, directoryName);
		if (!languageDirectory.exists()) {
			languageDirectory.mkdirs();
		}
		return languageDirectory;
	}

	/**
	 * @param source
	 * @param textFile
	 * @return
	 * @throws IOException 
	 */
	private TextFile copy(File source, TextFile textFile) throws IOException {
		if (source == null || !source.exists() || !source.isFile() || !source.canRead()) {
			throw new IllegalArgumentException("The source file has to be initiated with an existing, readable file to be copied.");
		}
		
		BufferedReader reader =null;
		BufferedWriter writer =null;
		TextFile currentTextFile = textFile;
		
		try {
			System.out.println("Input-File is: '"+source.getAbsolutePath()+"'");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(source),"UTF-8"));
			
			System.out.println(currentTextFile.getFileName()+"_"+new DecimalFormat("0000").format(currentTextFile.getFileCount())+currentTextFile.getFilesuffix());
			
			writer = this.renewWriter(writer,currentTextFile);
			
			// prepare number of file output streams depending on input file size
			// or returning only one file output stream if limitofbytes = 0
			// get number or recognize number to increase counter
			// change file output stream with every reach of limitofbytes
			// make BUFFER_SIZE configurable
			String line = null;
			Boolean flag_for_reaching_file_maximum = false;
			while ((line = reader.readLine()) != null) {
				
				if (flag_for_reaching_file_maximum && line.startsWith("<source><location>")) {
					currentTextFile = TextFile.next(currentTextFile);
					writer = this.renewWriter(writer,currentTextFile);
					flag_for_reaching_file_maximum = false;
				}
				
				if (FILE_SIZE_MAX > 0 && currentTextFile.getFile().length() > FILE_SIZE_MAX) {
					flag_for_reaching_file_maximum = true;
				}
				
				writer.write(line);
				writer.newLine();
				// sub optimal... this sucks big size. Another solution has to be found, otherwise the speed of this application is no where to be practical
				writer.flush();
				
			}
			
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
		return currentTextFile;
	}

	public boolean bzip_enabled() {
		return this.BZIP_FILE;
	}
	
	/**
	 * @param textFile
	 * @throws IOException 
	 */
	protected void bzip(File textFile) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(BZIP_EXECUTABLE+" "+textFile.getAbsolutePath());
		boolean processNotFinished = true;
		while (processNotFinished) {
			try {
				process.exitValue();
				processNotFinished = false;
			} catch (IllegalThreadStateException ex) {
				processNotFinished = true;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// does not matter
				}
			}
		}
	}

	/**
	 * @param channel
	 * @param inputFile
	 * @return 
	 * @throws IOException 
	 */
	private BufferedWriter renewWriter(BufferedWriter writer, TextFile inputFile) throws IOException {
		
		if (writer != null) {
			writer.flush();
			writer.close();
		}
		
		System.out.println("OutputFile is: '"+inputFile.getFile().getAbsolutePath()+"'");
		
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile.getFile()),"UTF-8"));
		
		return writer;
	}

	private TextFile createNewTextFile(File directory, String fileName, Integer count, String fileHash) throws IOException {
		return TextFile.newTextFile(directory, fileName, count, fileHash);
	}

	/**
	 * @param language
	 * @return
	 */
	private boolean checkLanguage(String language) {
		
		Pattern alphaSigns = Pattern.compile("^[a-z]{2,3}");
		Matcher matcher = alphaSigns.matcher(language);
		
		return (
				language != null &&
				!language.isEmpty() &&
				language.indexOf("-") == -1 &&
				language.indexOf("___") == -1 &&
				matcher.find()
				);
				
		
	}
	
}
