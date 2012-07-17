package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.util.regex.Pattern;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public interface CopyManager {

	public static final Pattern fileNamePattern = Textfile.inputFileNamePattern;

	void start();

	void stop();

	boolean isRunning();

	boolean isStoped();

	boolean isSuccessful();
	
	String getInstanceName();
	
	Configurator getConfigurator();
	
	MappingFactory getMappingFactory();

}