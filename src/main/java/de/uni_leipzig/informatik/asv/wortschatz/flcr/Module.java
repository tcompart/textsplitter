package de.uni_leipzig.informatik.asv.wortschatz.flcr;

public interface Module<T> {

	public static enum ModuleType {
		PRE_MODULE,
		POST_MODULE
	}
	
	ModuleType getType();
	
}
