/**
 * 
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

/**
 * @author <a href="mail:compart@informatik.uni-leipzig.de">Torsten Compart</a>
 * @param <Input> input parameter, which should be checked
 *
 */
public interface Filter<Input> {

	/**
	 * @param input - an input parameter, which should be checked if valid or not
	 * @return <code>true</code> if the assigned input parameter is accepted
	 */
	boolean apply(Input input);
	
}
