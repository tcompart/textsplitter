/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class LanguageDependentMover {
	
	protected StringLanguageParser languageParser;

	protected String directory_suffix = "";
	
	public LanguageDependentMover() {
		
		languageParser = new StringLanguageParser();
		languageParser.setPrefix("FL_");
		languageParser.setSuffix("0000.txt");		
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
	
	public File copyFile(File file, File directory) {
		
		String language = this.languageParser.updateName(file.getName());
		
		if (this.checkLanguage(language)) {
			File languageDirectory = new File(directory,language+directory_suffix);
			if (!languageDirectory.exists()) {
				languageDirectory.mkdirs();
			}
			
			File resultFile = this.createNewFile(languageDirectory, language+directory_suffix, 0);
			if (IOUtil.copy(file, resultFile)) {
				return resultFile;
			}
		}
		
		return null;
	}

	/**
	 * @param languageDirectory
	 * @param string
	 * @return
	 */
	private File createNewFile(File directory, String fileName, Integer count) {
		
		File file = new File(directory, fileName+"_"+new DecimalFormat("0000").format(count)+".txt");
		while (file.exists()) {
			file = this.createNewFile(directory, fileName, ++count);
		}
		return file;
	}

	/**
	 * @param language
	 * @return
	 */
	private boolean checkLanguage(String language) {
		
		return (
				language != null &&
				!language.isEmpty() &&
				language.indexOf("-") == -1 &&
				language.indexOf("___") == -1
				);
				
		
	}
	
}
