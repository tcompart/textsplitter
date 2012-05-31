package de.uni_leipzig.informatik.asv.wortschatz.flcr.task;


public abstract class Check<T> extends BasicListenerClass {

	public abstract void validate(T input) throws CheckFailedException;

}