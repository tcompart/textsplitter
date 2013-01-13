package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * <b>Checksum SHA1</b>
 * <p>
 * Source can be found: <a href="http://www.rgagnon.com/javadetails/java-0416.html">Real's Java How-To</a>
 * </p>
 * Create the SHA1 checksum.
 * 
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 * @version 0.01 10:00 (Leipzig) (2011/08/02)
 */
public class Checksum {

	private static HashMap<String,Checksum> map = new HashMap<String,Checksum>();
	
	private MessageDigest complete = null;
	
	private Checksum(String algorithm) throws NoSuchAlgorithmException {
		complete = MessageDigest.getInstance(algorithm);		
	}
	
	
	/**
	 * @param file - the concrete file object of class {@link File}
	 * @return a byte array containing the HEX values of the SHA1 checksum
	 * @throws IOException if the algorithm or file created an error. The algorithm can create an error, if the MD5 or SHA1 algorithm could not be found.
	 * The file can create an error, if it could not be found, or the file is corrupted and therefore not readable.
	 */
	private byte[] createChecksum(File file) throws IOException {
		InputStream fis = new FileInputStream(file);
		
		byte[] buffer = new byte[1024];
		
		int numRead;
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}

	/**
	 * @param byteArray - the concrete byte array received by {@link File} object
	 * @return a byte array containing the HEX values of the SHA1 checksum
	 */
	private String generateString(byte[] byteArray) {
		String result = "";
		for (int i = 0; i < byteArray.length; i++) {
			result += Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	
	/**
	 * @param file - instance of class {@link File}, which points to a concrete physical saved file object
	 * @return the SHA1 checksum of the assigned 'file' (class {@link File})
	 * @throws IOException if the algorithm or file created an error. The algorithm can create an error, if the MD5 or SHA1 algorithm could not be found.
	 * The file can create an error, if it could not be found, or the file is corrupted and therefore not readable.
	 */
	public static String getChecksumSHA1(File file) throws IOException {
		
		Checksum SHA1 = null;
		if (map.containsKey("sha1")) {
			SHA1 = map.get("sha1");
		}
		
		if (SHA1 == null) {
			try {
				SHA1 = new Checksum("sha1");
			} catch (NoSuchAlgorithmException e) {
				throw new IOException("No such algorithm: SHA1",e);
			}
		}
		
		byte[] b = SHA1.createChecksum(file);
		return SHA1.generateString(b);
	}
	
	/**
	 * @param file - instance of class {@link File}, which points to a concrete physical saved file object
	 * @return the SHA1 checksum of the assigned 'file' (class {@link File})
	 * @throws IOException if the algorithm or file created an error. The algorithm can create an error, if the MD5 or SHA1 algorithm could not be found.
	 * The file can create an error, if it could not be found, or the file is corrupted and therefore not readable.
	 */
	public static String getChecksumMD5(File file) throws IOException {
		
		Checksum MD5 = null;
		if (map.containsKey("md5")) {
			MD5 = map.get("md5");
		}
		
		if (MD5 == null) {
			try {
				MD5 = new Checksum("md5");
			} catch (NoSuchAlgorithmException e) {
				throw new IOException("No such algorithm: MD5",e);
			}
		}
		
		byte[] b = MD5.createChecksum(file);
		return MD5.generateString(b);
	}
	
	
	
}