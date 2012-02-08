/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.util;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class StringLanguageParser {

	private Prefix prefix;
	private Suffix suffix;
	
	/**
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		if (prefix != null && prefix.length() > 0) {
			this.prefix = new Prefix(prefix);
		}
	}
	
	/**
	 * @param suffix
	 */
	public void setSuffix(String suffix) {
		if (suffix != null && suffix.length() > 0) {
			this.suffix = new Suffix(suffix);
		}
	}

	/**
	 * @param name
	 * @return
	 */
	public String updateName(String name) {
		
		if (this.prefix != null && this.prefix.getPrefix().length() > 0 && this.prefix.getPrefix().length() < name.length()) {
			name = this.reduceFileName(name, this.prefix);
		}
		if (this.suffix != null && this.suffix.getSuffix().length() > 0 && this.suffix.getSuffix().length() < name.length()) {
			name = this.reduceFileName(name, this.suffix);
		}
		return name;
	}
	
	/**
	 * @param name
	 * @param prefix
	 * @return
	 */
	private String reduceFileName(String name, Prefix prefix) {
		return name.substring(prefix.getPrefix().length());
	}
	
	

	/**
	 * @param name
	 * @param suffix
	 * @return
	 */
	private String reduceFileName(String name, Suffix suffix) {
		return name.substring(0, name.length()-suffix.getSuffix().length());
	}



	private static class Prefix {
		
		private String prefix;

		public Prefix(String prefix) {
			this.prefix = prefix;
		}
		
		String getPrefix() {
			return prefix;
		}
		
	}
	
	private static class Suffix {
		
		private String suffix;

		public Suffix(String suffix) {
			this.suffix = suffix;
		}
		
		String getSuffix() {
			return suffix;
		}
	}
	
	/**
	 * Intention is to mark the error if a language could not be found.
	 * 
	 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
	 *
	 */
	public static class LanguageNotFoundException extends Exception {
		
	}

}
