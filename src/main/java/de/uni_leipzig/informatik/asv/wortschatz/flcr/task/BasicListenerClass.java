package de.uni_leipzig.informatik.asv.wortschatz.flcr.task;

import de.compart.common.event.Event;
import de.compart.common.event.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BasicListenerClass {

	private final Set<EventListener> listeners = new HashSet<EventListener>();

	public void addEventListener( @NotNull EventListener listener ) {
		this.listeners.add( listener );
	}

	public void notify( final Event event ) {
		for ( EventListener listener : this.listeners ) {
			listener.listen( event );
		}
	}

}
