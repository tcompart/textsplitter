/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.exception;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class ExitException extends SecurityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public final int status;
	
    /**
     * @param status
     */
    public ExitException(int status) {
            super("There is no escape!");
            this.status = status;
    }
	
}
