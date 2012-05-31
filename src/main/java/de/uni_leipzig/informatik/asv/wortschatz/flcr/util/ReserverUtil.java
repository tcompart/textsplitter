package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.asv.clarin.common.tuple.Maybe;

public class ReserverUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ReserverUtil.class);
	
	private static final BlockingQueue<File> collection = new LinkedBlockingQueue<File>(10);

	public synchronized static Maybe<File> reserve(final File outputFile) throws InterruptedException {
		log.info(String.format("Reserving file '%s'.", outputFile.getName()));
		while (collection.contains(outputFile)) {
			log.debug(String.format("Still reserved: file '%s'. Keep waiting.", outputFile.getName()));
			Thread.sleep(1000);
		}
		collection.put(outputFile);
		log.debug(String.format("Reserved file '%s'.", outputFile.getName()));
		return Maybe.just(outputFile);
	}

	public synchronized static boolean release(final File inputFile) {
		if (collection.contains(inputFile)) {
			log.info(String.format("Releasing file '%s'.", inputFile.getName()));
			return collection.remove(inputFile);
		} else if (log.isWarnEnabled()) {
			log.warn(String.format("Unable to release file '%s', because it was not reserved!", inputFile.getName()));
		}
		return true;
	}

	public synchronized static int numberOfReservedFiles() {
		return collection.size();
	}	
	
	

}
