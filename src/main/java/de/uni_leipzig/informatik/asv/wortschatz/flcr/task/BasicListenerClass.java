package de.uni_leipzig.informatik.asv.wortschatz.flcr.task;

import java.util.HashSet;
import java.util.Set;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.EventListener;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.EventListener.Event;

public class BasicListenerClass {

	private final Set<EventListener> listeners = new HashSet<EventListener>();

	public void addEventListener(EventListener listener) {
		if (listener == null) { throw new NullPointerException(); }
		this.listeners.add(listener);
	}

	public void notify(final Event event) {
		for (EventListener listener : this.listeners) {
			listener.listen(event);
		}
	}

}
