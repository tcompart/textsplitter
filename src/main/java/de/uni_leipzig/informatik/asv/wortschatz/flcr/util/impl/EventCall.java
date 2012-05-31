package de.uni_leipzig.informatik.asv.wortschatz.flcr.util.impl;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.EventListener.Event;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.EventListener.EventType;

public abstract class EventCall implements Event {

	private final String sourceName;

	public EventCall(Object sourceInput) {
		this.sourceName = sourceInput.toString();
	}

	@Override
	public EventType getType() {
		return EventType.CALL;
	}

	@Override
	public String getSource() {
		return this.sourceName;
	}

}
