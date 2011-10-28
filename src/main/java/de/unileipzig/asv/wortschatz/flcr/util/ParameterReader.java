/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.util;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class ParameterReader {

	
	public ParameterResult parse(String parameterString) {		
		return this.parse(parameterString.split(" "));
	
	}

	public ParameterResult parse(String[] parameterList) {
		return this.parse(new LinkedList<String>(Arrays.asList(parameterList)));
	}
	
	public ParameterResult parse(LinkedList<String> parameterList) {
		
		ParameterResultLinking result = new ParameterResultLinking();
		
		String parameter = null;
		for (int i = 0; i < parameterList.size(); i++) {
			String currentEntry = parameterList.get(i);
			if (currentEntry.startsWith("--")) {
				parameter = currentEntry;
				result.addParameter(parameter);
			} else {
				result.addValue(parameter, currentEntry);
			}
		}
		
		return result;		
	}
}
