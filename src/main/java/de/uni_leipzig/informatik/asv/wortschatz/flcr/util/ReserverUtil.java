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

	public synchronized static Maybe<File> reserve(final File outputFile) {
		log.info("Reserving file '{}'.", outputFile.getName());
		if (!collection.contains(outputFile) && collection.offer(outputFile)) {
			log.debug("Reserved file '{}'.", outputFile.getName());
			return Maybe.just(outputFile);
		}
		log.debug("File '{}' is still reserved.", outputFile.getName());
		return Maybe.nothing();
	}

	public static boolean release(final File inputFile) {
		return collection.remove(inputFile);
	}

	public synchronized static int numberOfReservedFiles() {
		return collection.size();
	}	
	
	

}
