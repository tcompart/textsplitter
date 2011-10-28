/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class IOUtil {
	
	public static File createDirectory(String directory) {
		File dir = new File(directory);
		dir.mkdirs();
		return dir;
	}
	

	public static File createFile(File directory, String fileName) throws IOException {
		if (directory.exists()) {
			File file = new File(directory, fileName);
			file.createNewFile();
			return file;			
		}
		throw new NotExistingDirectoryException(directory.getAbsoluteFile()+" does not exist. Unable to create a file in the directory.");
	}	
	
	public static boolean removeDirectory(File directory) {
		if (directory.isDirectory()) {
			boolean flag = true;
			for (File file : directory.listFiles()) {
				if (file.isDirectory()) {
					if (!removeDirectory(file)) {
						flag = false;
						continue;
					} 
				} 
				if (!removeFile(file)) {
					flag = false;
				}
			}
			return flag;
		}
		return false;
	}
	
	public static boolean removeFile(File file) {
		return (file != null && file.delete());
	}
	
	/**
	 * This method copies the content from the 'source' {@link File} to the 'destination' {@link File}.
	 * The 'source' will remain at its place and will not be moved.
	 * 
	 * @param source - the source file (instance of class {@link File})
	 * @param destination - the destination file (instance of class {@link File})
	 * @return <code>true</code> if the content of 'source' could be copied to 'destination'.
	 */
	public static boolean copy(File source, File destination) {
		
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
			
			byte[] buffer = new byte[4096];
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
}
