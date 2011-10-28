/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.listener;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public interface EventListener {

	public enum EventType {
		CALL,
		ERROR,
		EXIT
	}
	
	public interface Event {
		
		public EventType getType();
		
		public String getSource();
		
		public String getMessage();
		
	}
	
	public void listen(Event event);
	
}
