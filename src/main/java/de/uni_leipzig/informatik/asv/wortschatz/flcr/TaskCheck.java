package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Check;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.CheckFailedException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class TaskCheck extends Check<Set<Task>> {

	private static Logger log = LoggerFactory.getLogger( TaskCheck.class );

	public static void validate( final Task inputTask ) throws CheckFailedException {
		if ( inputTask != null && !inputTask.finished() ) {
			log.warn( "Assigned task '{}' not finished yet.", inputTask.getUniqueIdentifier() );
		} else if ( inputTask != null && inputTask.successful() ) {
			log.info( "Assigned task '{}' finished successfully.", inputTask.getUniqueIdentifier() );
		}
		if ( inputTask == null || !inputTask.finished() || !inputTask.successful() ) {
			throw new CheckFailedException();
		}
	}

	@Override
	public void validate( final Set<Task> inputSet )
			throws CheckFailedException {
		if ( inputSet == null ) {
			throw new NullPointerException();
		}
		for ( Task task : inputSet ) {
			validate( task );
		}
	}

}