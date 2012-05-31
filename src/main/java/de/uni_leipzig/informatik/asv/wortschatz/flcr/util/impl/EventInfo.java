package de.uni_leipzig.informatik.asv.wortschatz.flcr.util.impl;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.EventListener.Event;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.EventListener.EventType;

public abstract class EventInfo implements Event {

	private final String sourceName;

	public EventInfo(Object sourceInput) {
		this.sourceName = sourceInput.toString();
	}

	@Override
	public EventType getType() {
		return EventType.INFO;
	}

	@Override
	public String getSource() {
		return this.sourceName;
	}

}
