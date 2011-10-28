/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import de.unileipzig.asv.wortschatz.flcr.util.ParameterReader;
import de.unileipzig.asv.wortschatz.flcr.util.ParameterResult;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class WortschatzResourceStart {

	public static final String PROGRAM_NAME = "flcr - Findlinks & WebCrawl Text File Movement";
	
	public static final String VERSION = "0.0.1-SNAPSHOT";
	
	public static final String propertyOutputDirectoryName = "flcr.output.directory";
	
	public static final String propertyOutputDirectoryDefaultValue = "flcr";

	public static final String propertyWebCrawlDirectorySuffix = "flcr.output.webcrawl.suffix";
	
	public static final String propertyWebCrawlDirectorySuffixDefaultValue = "_webcr_2011";

	public static final String propertyFindlinksDirectorySuffix = "flcr.output.findlinks.suffix";

	public static final String propertyFindlinksDirectorySuffixDefaultValue = "_webfl_2011";

	private static String propertyFileName = "properties.prop";
	
	private static List<String> findLinksInputDirectories = new ArrayList<String>();
	
	private static List<String> webcrawlInputDirectories = new ArrayList<String>();

	private static String resultOutputDirectory;

	private static boolean DRYRUN = false;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		findLinksInputDirectories = new ArrayList<String>();
		webcrawlInputDirectories = new ArrayList<String>();
		resultOutputDirectory = null;
		
		checkParameter(new LinkedList<String>(Arrays.asList(args)));
		runProgram();
	}

	/**
	 * @param list
	 */
	private static void checkParameter(LinkedList<String> list) {
		
		ParameterResult result = new ParameterReader().parse(list);
		
		if (result.hasParameter("--help")) {
			printHelp();
			System.exit(0);			
		} else {
			if (!result.hasParameter("--findlinks")) {
				throw new RuntimeException("The findlinks directories have to be defined. Please use parameter '--findlinks' for the association.");
			} else if (!result.hasValue("--findlinks")) {
				throw new RuntimeException("The findlinks parameter does not have any file paths associated. You can append as many directories as you want. But the associated file paths have to point to directories.");
			}
			for (String directory : result.getValues("--findlinks")) {
				System.out.println(directory);
			}
			findLinksInputDirectories.addAll(result.getValues("--findlinks"));
			
			if (!result.hasParameter("--webcrawl")) {
				throw new RuntimeException("The webcrawl directories have to be defined. Please use parameter '--webcrawl' for the association.");
			} else if (!result.hasValue("--webcrawl")) {
				throw new RuntimeException("The webcrawl parameter does not have any file paths associated. You can append as many directories as you want. But the associated file paths have to point to directories.");
			}
			webcrawlInputDirectories.addAll(result.getValues("--webcrawl"));
			
			if (result.hasParameter("--properties") && result.hasValue("--properties")) {
				propertyFileName = result.getValues("--properties").get(0);
			}
			
			if (result.hasParameter("--output") && result.hasValue("--output")) {
				resultOutputDirectory = result.getValues("--output").get(0);
			}
			
			if (result.hasParameter("--dryrun")) {
				DRYRUN = true;
			}
			
		}
	}

	/**
	 * @return
	 */
	public static String printIntro() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Program: ----- "+PROGRAM_NAME+" -----\n");
		sb.append("Version: "+VERSION+"\n");
		sb.append("\n");
		
		System.out.println(sb.toString());
		
		return sb.toString();
	}
	
	/**
	 * @return 
	 * 
	 */
	public static String printHelp() {
		printIntro();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Supported Parameters:\n");
		sb.append("\t--help\t\t:\tthis help screen.\n");
		sb.append("\t--findlinks\t:\ta list of directories, which should be used as input for the findlinks data.\n");
		sb.append("\t--webcrawl\t:\ta list of directories, which should be used as input for the web crawl data.\n");
		sb.append("\t--output\t:\ta file path were the output directory should be created.\n");
		sb.append("\t--properties\t:\tpath to a property file; default: properties.prop. Those are loaded before the start. However highest priority have directly called parameters and their values. See information below of the possible properties and their default values.\n");
		sb.append("\t--dryrun\t:\tfor testing purpose. This creates instances but the copy process is not started. This enables the check of parameters and properties.\n");
		sb.append("\n");
		sb.append("Supported Properties:\n");
		sb.append("\t"+propertyOutputDirectoryName+": '"+propertyOutputDirectoryDefaultValue+"'\n");
		sb.append("\t"+propertyWebCrawlDirectorySuffix+": '"+ propertyWebCrawlDirectorySuffixDefaultValue+"'\n");
		sb.append("\t"+propertyFindlinksDirectorySuffix+": '"+propertyFindlinksDirectorySuffixDefaultValue+"'\n");
		sb.append("\n");
		System.out.println(sb.toString());
		
		return sb.toString();
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private static void runProgram() throws IOException {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertyFileName));
			System.out.println("Loaded successfully properties from file: "+propertyFileName);
		} catch (FileNotFoundException e) {
			System.err.println("Property file '"+propertyFileName+"' could not be found. Using default values.");
		}
		WortschatzResourceStart wsr = new WortschatzResourceStart(properties);
		
		if (!DRYRUN) {
			wsr.createFindlinks();
			wsr.createWebCrawl();
		}
	}

	private String outputDirectoryName;
	private String webcrawlDirectorySuffix;
	private String findlinksDirectorySuffix;

	/**
	 * @param properties
	 */
	public WortschatzResourceStart(Properties properties) {
		webcrawlDirectorySuffix = properties.getProperty(propertyWebCrawlDirectorySuffix, propertyWebCrawlDirectorySuffixDefaultValue);
		findlinksDirectorySuffix = properties.getProperty(propertyFindlinksDirectorySuffix, propertyFindlinksDirectorySuffixDefaultValue);
		
		if (resultOutputDirectory != null) {
			outputDirectoryName = resultOutputDirectory;
		} else {
			outputDirectoryName = properties.getProperty(propertyOutputDirectoryName, propertyOutputDirectoryDefaultValue);			
		}
		
		if (!this.checkDirectories(findLinksInputDirectories)) {
			throw new IllegalArgumentException("The assigned values for the parameter --findlinks are no valid directory paths.");
		}
		if (!this.checkDirectories(webcrawlInputDirectories)) {
			throw new IllegalArgumentException("The assigned values for the parameter --webcrawl are no valid directory paths.");			
		}
		
	}

	/**
	 * @param directories
	 */
	private boolean checkDirectories(List<String> directories) {
		for (String directory : directories) {
			System.out.println(directory);
			File dir = new File(directory);
			if (!dir.exists() || !dir.isDirectory()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return
	 */
	private void createWebCrawl() {
		LanguageDirectoryMover webcrawl = new LanguageDirectoryMover();
		webcrawl.setDirectorySuffix(webcrawlDirectorySuffix);
		webcrawl.setOutputDirectory(new File(outputDirectoryName));
		
		for (String directory : webcrawlInputDirectories) {
			webcrawl.addInputDirectory(new File(directory));
		}
		
		webcrawl.searchDirectories();
	}

	/**
	 * @return
	 */
	private void createFindlinks() {
		LanguageDirectoryMover findLinks = new LanguageDirectoryMover();
		findLinks.setDirectorySuffix(findlinksDirectorySuffix);
		findLinks.setOutputDirectory(new File(outputDirectoryName));
		
		for (String directory : findLinksInputDirectories) {
			findLinks.addInputDirectory(new File(directory));
		}
		
		findLinks.searchDirectories();
		
	}
	
}
