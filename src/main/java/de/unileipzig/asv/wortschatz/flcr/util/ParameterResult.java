/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.util;

import java.util.List;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public interface ParameterResult {

	boolean hasParameter(String parameter);
	
	boolean hasValue(String parameter);
	
	List<String> getValues(String parameter);
}
