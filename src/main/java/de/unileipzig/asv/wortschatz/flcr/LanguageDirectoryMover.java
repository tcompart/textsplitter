/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class LanguageDirectoryMover extends LanguageDependentMover {

	private List<File> directories = new ArrayList<File>();
	private File output;
	
	public LanguageDirectoryMover() {
		
	}
	
	public void addInputDirectory(File directory) {
		if (directory == null || !directory.isDirectory()) {
			throw new IllegalArgumentException("The assigned file descriptor has to point to a directory.");
		}
		
		this.directories.add(directory);
		
	}
	
	public List<File> getInputDirectories() {
		return this.directories;
	}
	
	public void setOutputDirectory(File directory) {
		if (directory == null) {
			throw new IllegalArgumentException("The directory has to be initialized.");
		}
		directory.mkdirs();
		this.output = directory;
	}
	
	public File getOutputDirectory() {
		return this.output;
	}
	
	public void searchDirectories() {
		for (File directory : this.directories) {			
			for (File file : directory.listFiles()) {
				this.copyFile(file, this.getOutputDirectory());
			}
		}		
	}
	
}
