/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr;

import java.io.IOException;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class NotExistingDirectoryException extends IOException {

	/**
	 * @param string
	 */
	public NotExistingDirectoryException(String string) {
		super(string);
	}

}
