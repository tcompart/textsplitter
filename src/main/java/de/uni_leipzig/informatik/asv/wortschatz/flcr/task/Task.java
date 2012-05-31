package de.uni_leipzig.informatik.asv.wortschatz.flcr.task;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Command.CommandExecutionException;

public class Task extends BasicListenerClass {

	private static final Logger log = LoggerFactory.getLogger(Task.class);

	private static final AtomicLong atomicNumber = new AtomicLong(0);

	private boolean finished;
	private boolean successful;

	/*
	 * i've choosen long because a very large number of task
	 * can be expected... int could possible break. However,
	 * more bytes are needed, and we speak only about an identifier
	 */
	private final long taskNumber;

	private final Command command;

	public Task(Command inputCommand) {
		this.finished = false;
		this.successful = false;
		this.taskNumber = atomicNumber.incrementAndGet();
		this.command = inputCommand;
		log.info(String.format("Initializing [%s_%d]", Task.class.getSimpleName(), this.taskNumber));
	}

	public String getUniqueIdentifier() {
		return String.valueOf(this.taskNumber);
	}

	public void doTask() throws IllegalStateException {

		if (this.command == null) {
			log.error(String.format(
					"[%s_%d]: not correctly initialized without an command to be executed.",
					Task.class.getSimpleName(), this.taskNumber));
			throw new IllegalStateException();
		}

		try {
			if (log.isInfoEnabled()) {
				log.info(String.format("[%s_%d]: executing command.", Task.class.getSimpleName(), this.taskNumber));
			}
			this.command.execute();
			this.successful = true;
		} catch (CommandExecutionException ex) {
			throw new RuntimeException(String.format(
					"[%s_%d]: failed command execution.", Task.class.getSimpleName(), this.taskNumber), ex);
		}

		this.finished = true;

	}

	public boolean successful() {
		return this.successful;
	}

	public boolean finished() {
		return this.finished;
	}

}