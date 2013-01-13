package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * User: torsten
 * Date: 2013/01
 * Time: 01:25
 *
 */
public class Pair<S, T> {

	//============================== CLASS VARIABLES ================================//
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( Pair.class );
	//=============================== CLASS METHODS =================================//
	//===============================  VARIABLES ====================================//
	private final S first;
	private final T second;

	//==============================  CONSTRUCTORS ==================================//
	public Pair( @NotNull final S first, @Nullable final T second ) {
		this.first = first;
		this.second = second;
	}

	//=============================  PUBLIC METHODS =================================//
	public S getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	public boolean hasSecond() {
		return this.getSecond() != null;
	}

	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//
}