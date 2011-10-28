/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class ParameterResultLinking implements ParameterResult {

	private HashMap<String,List<String>> parameters = new HashMap<String,List<String>>();
	
	@Override
	public boolean hasParameter(String parameter) {
		return (this.parameters.containsKey(parameter));
	}

	@Override
	public boolean hasValue(String parameter) {
		return (this.hasParameter(parameter) && !this.getValues(parameter).isEmpty());
	}

	@Override
	public List<String> getValues(String parameter) {
		if (!this.hasParameter(parameter)) {
			return new ArrayList<String>();
		}
		return (this.parameters.get(parameter));
	}

	/**
	 * @param parameter
	 */
	public void addParameter(String parameter) {
		this.parameters.put(parameter, new ArrayList<String>());
	}
	
	/**
	 * @param parameter
	 * @param value
	 */
	public void addValue(String parameter, String value) {
		this.getValues(parameter).add(value);
	}

}
