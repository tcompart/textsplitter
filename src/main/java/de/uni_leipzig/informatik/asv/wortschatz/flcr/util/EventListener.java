/**
 * 
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 * 
 */
public interface EventListener {

	public enum EventType {
		/**
		 * an information
		 */
		INFO,
		/**
		 * an information, which should be handled like a message call (indirect
		 * call)
		 */
		CALL,
		/**
		 * an error occurred, which should be distributed to all listeners
		 */
		ERROR,
		/**
		 * a message, which signals the last message or event, and should turn
		 * off every listener
		 */
		EXIT
	}

	public interface Event {

		public EventType getType();

		public String getSource();

		public String getMessage();

	}

	public void listen(Event event);

}
