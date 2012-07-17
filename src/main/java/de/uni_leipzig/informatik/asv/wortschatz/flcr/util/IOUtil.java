/**
 * 
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class IOUtil {
	
	private static final Logger log = LoggerFactory.getLogger(IOUtil.class);
	
	private static final int BUFFER_SIZE = 4096;

	private static final Integer maximumTries = 5;
	
	public static boolean createDirectory(File directory) {
		int currentNumberOfTries = 0;
		while (!directory.exists()) {
			if (!directory.mkdirs() && maximumTries > currentNumberOfTries) {
				currentNumberOfTries++;
				log.debug(String.format("Directory '%s' not created yet!", directory));
			} else if (maximumTries <= currentNumberOfTries) {
				return false;
			}
		}
		return true;
	}
	

	public static File createFile(File directory, String fileName) throws IOException {
		if (directory.exists()) {
			File file = new File(directory, fileName);
			file.createNewFile();
			return file;			
		}
		throw new IOException(directory.getAbsoluteFile()+" does not exist. Unable to create a file in the directory.");
	}	
	
	public static boolean removeDirectory(File directory) {
		boolean flag = true;
		if (directory.exists() && directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				if (file.isDirectory()) {
					flag = removeDirectory(file) & flag;
				} else {
					flag = removeFile(file) & flag;
				}
			}
			flag = directory.delete() & flag;
		}
		return flag;
	}
	
	public static boolean removeFile(File file) {
		return (file != null && file.delete());
	}
	
	public static Collection<File> getFiles(final File inputDirectory, final boolean recursive) {
		final Collection<File> resultSet = new HashSet<File>();
		
		if (inputDirectory.isDirectory()) {
			for (File innerFile : inputDirectory.listFiles()) {
				if (innerFile.isFile()) {
					resultSet.add(innerFile);
				} else if (innerFile.isDirectory() && recursive) {
					resultSet.addAll(getFiles(innerFile, recursive));
				}
			}
		} else if (inputDirectory.isFile()) {
			resultSet.add(inputDirectory);
		}
		
		return Collections.unmodifiableCollection(resultSet);
	}
	
	/**
	 * This method copies the content from the 'source' {@link File} to the 'destination' {@link File}.
	 * The 'source' will remain at its place and will not be moved.
	 * 
	 * @param source - the source file (instance of class {@link File})
	 * @param destination - the destination file (instance of class {@link File})
	 * @return <code>true</code> if the content of 'source' could be copied to 'destination'.
	 */
	public static boolean copy(File source, File destination, long limitInBytes) {
		
		if (source == null || !source.exists() || !source.isFile() || !source.canRead()) {
			throw new IllegalArgumentException("The source file has to be initiated with an existing, readable file to be copied.");
		}
		
		/*
		 * here should be thoughts if the 'destination' file exists
		 */
		
		FileInputStream fis =null;
		FileOutputStream fos =null;
		
		try {
			fis =new FileInputStream(source);
			fos =new FileOutputStream(destination);
			// prepare number of file output streams depending on input file size
			// or returning only one file output stream if limitofbytes = 0
			// get number or recognize number to increase counter
			// change file output stream with every reach of limitofbytes
			// make BUFFER_SIZE configurable
			byte[] buffer = new byte[BUFFER_SIZE];
			int read;
			while ((read =fis.read(buffer)) != -1) {
				fos.write(buffer,0,read);
			}
		} catch (IOException e) {
			return false;
		} finally {
				try {
					if (fis != null) {
						fis.close();
					}
					if (fos !=null) {
						fos.close();
					}
				} catch (IOException e) {
					return false;
				}
		}
		return true;
	}
		
	public static boolean copy(File source, File destination) {
		return copy(source, destination, 0);
	}	
	
}
