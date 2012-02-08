/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

import de.unileipzig.asv.wortschatz.flcr.util.Checksum;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class TextFile {

	private static final String fileSuffix = ".txt";
	
	private String fileName;
	
	private Integer fileCount = 0;

	private File directory;
	
	private File that;

	private final String fileHashSourceFile;
	
	/**
	 * @return an instance of class {@link TextFile}
	 * @throws IOException error occurs because the newly created file path could not be used for a new file pointer (creating a new file at the file system block)
	 */
	public static TextFile next(TextFile textFile) throws IOException {
		return newTextFile(textFile.getDirectory(), textFile.getFileName(), textFile.getFileCount(), textFile.getFileHashSourceFile());
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	public static TextFile newTextFile(File directory, String fileName, Integer fileCount, String fileHash) throws IOException {
		File file = new File(directory,fileName+"_"+new DecimalFormat("0000").format(fileCount)+fileSuffix);
		
		if (fileKnown(file, fileHash)) {
			return newTextFile(directory, fileName,++fileCount, fileHash);			
		} else {
			TextFile textFile = new TextFile(fileName, fileCount, directory, fileHash);
			textFile.that = file;
			file.createNewFile();
			return textFile;
		}
	}

	/**
	 * @param file - current file pointer
	 * @param fileHashSourceFile - file hash of the source file if present
	 * @return <code>true</code> if the file does exist already and the checksum of the source file is equals the newly generated hash file (probably the same files...)
	 * @throws IOException 
	 */
	protected static boolean fileKnown(File file, String fileHashSourceFile) throws IOException {		
		if (!file.exists()) {
			return false;
		} else {
			if (fileHashSourceFile != null && Checksum.getChecksumSHA1(file).equals(fileHashSourceFile)) {
				return true;
			}
		}
		return true;
	}

	protected TextFile(String fileName, Integer fileCount, File directory) throws IOException {
		this(fileName, fileCount, directory, null);
	}
	
	protected TextFile(String fileName, Integer fileCount, File directory, String fileHash) throws IOException {
		this.setFileName(fileName);
		this.setFileCount(fileCount);
		this.directory = directory;
		this.fileHashSourceFile = fileHash;
		
	}
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	protected void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileCount
	 */
	public Integer getFileCount() {
		return fileCount;
	}

	protected void setFileCount(Integer fileCount) {
		this.fileCount = fileCount;
	}
	
	/**
	 * @return the filesuffix
	 */
	public String getFilesuffix() {
		return fileSuffix;
	}
	
	public long getSize() {
		return this.getFile().length();
	}
	
	public File getFile() {
		return that;
	}
	
	public File getDirectory() {
		return directory;
	}

	/**
	 * @return
	 */
	public String getFileHashSourceFile() {
		return this.fileHashSourceFile;
	}
}
