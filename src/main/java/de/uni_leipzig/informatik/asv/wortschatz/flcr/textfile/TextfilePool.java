package de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.SelectorPool;

public class TextfilePool extends SelectorPool<TextFile> {

	private static Logger log = LoggerFactory.getLogger(TextfilePool.class);

	private Iterator<File> unusedfiles;

	private final int maximum;

	public TextfilePool(Collection<File> inputFiles) {
		super(TextfilePool.class.getSimpleName(), 5);
		unusedfiles = inputFiles.iterator();
		maximum = inputFiles.size();
	}

	@Override
	protected synchronized TextFile create() throws ReachedEndException {
		if (unusedfiles == null || !unusedfiles.hasNext()) { throw new ReachedEndException(); }

		File fileToBeUsed = unusedfiles.next();

		if (fileToBeUsed == null) { // catch an unlikely case, but it should be
									// checked before strange null pointer
									// exceptions occur
			throw new RuntimeException(
					String
							.format(
									"This case should not happen. Class '%s' allowed the access for the next instance of class '%s'. Therefore a valid return valid was expected, however the returned value points to null.",
									TextfilePool.class.getSimpleName(), TextFile.class.getSimpleName()));
		}

		try {
			return new TextFile(fileToBeUsed);
		} catch (IOException ex) {
			log.warn(String.format(
					"Unable to create another %s, because file '%s'", TextFile.class.getSimpleName(),
					fileToBeUsed.getName()));
			throw new ReachedEndException("An IOException was thrown, which indicates the possible end of the pool", ex);
		}
	}

	@Override
	public boolean validate(TextFile textFile ) {
		try {
			return textFile != null && textFile.getFile() != null && textFile.getFile().exists()
					&& textFile.getOutputType() != null;
		} catch (FileNotFoundException e) {
			log.error(String.format(
					"The validation failed, because the validated %s has no valid file associated.",
					TextFile.class.getSimpleName()));
			return false;
		}
	}

	@Override
	protected synchronized void releaseObj(TextFile textFile ) {
		if ( textFile != null) {
			textFile.release();
		}
	}

	@Override
	public int size() {
		return this.maximum;
	}

}
