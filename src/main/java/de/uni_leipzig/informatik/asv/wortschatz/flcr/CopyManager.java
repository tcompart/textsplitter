package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.PrintStream;
import java.util.regex.Pattern;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public interface CopyManager {

	public static final Pattern fileNamePattern = TextFile.inputFileNamePattern;

	void start();

	void stop();

	boolean isRunning();

	boolean isStopped();

	boolean isSuccessful();
	
	String getInstanceName();
	
	Configurator getConfigurator();
	
	MappingFactory getMappingFactory();

	boolean hasModule(final Module<?> module);
	
	void addModule(final Module<?> module);
	
	void removeModule(final Module<?> module);

	void setOutputStream(PrintStream out);
	
}