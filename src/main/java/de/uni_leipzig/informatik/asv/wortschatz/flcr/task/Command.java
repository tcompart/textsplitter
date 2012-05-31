package de.uni_leipzig.informatik.asv.wortschatz.flcr.task;

import java.io.IOException;

public interface Command {

	public void execute() throws CommandExecutionException;

	public static class CommandExecutionException extends IOException {

		public CommandExecutionException(Throwable inputEx) {
			super(inputEx);
		}

		public CommandExecutionException(String inputMessage, Throwable inputEx) {
			super(inputMessage, inputEx);
		}

		private static final long serialVersionUID = 1L;
	}

}
