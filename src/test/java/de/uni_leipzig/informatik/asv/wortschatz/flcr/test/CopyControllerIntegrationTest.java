package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.hamcrest.Matcher;
import org.junit.Test;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReachedEndException;

public class CopyControllerIntegrationTest extends CopyControllerUnitTest {

	@Test
	public void copyFullFileSet() throws IOException, InterruptedException {
		this.create();

		Collection<File> fileSet = new HashSet<File>();
		fileSet.add(textfileFile);
		controller.start(fileSet);

		assertThat(controller.isRunning(), is(true));
		assertThat(controller.isStoped(), is(false));

		synchronized (this) {
			this.wait(3000);
		}
		
		final Textfile textfile = new Textfile(textfileFile);

		final File resultingTextfile = factory.getDefaultTextfileMapping(textfile);
		
		while (!resultingTextfile.exists()) {
			synchronized (this) {
				wait(100);
			}
		}
		
		assertThat(resultingTextfile.exists(), is(true));
		
		assertSame(textfile, new Textfile(resultingTextfile));

	}

	private static void assertSame(final Textfile inputOriginal, final Textfile inputChanged)
			throws FileNotFoundException {
		assertThat(inputOriginal, notNullValue());
		assertThat(inputChanged, notNullValue());

		assertThat(inputOriginal.getFile().getAbsolutePath(), 
					inputOriginal.getOutputType(), is(inputChanged.getOutputType()));
		assertThat(inputOriginal.getYear(), is(inputChanged.getYear()));
		assertThat(inputOriginal.getLanguage(), is(inputChanged.getLanguage()));
		assertThat(inputOriginal.getNumberOfSources(), is(inputChanged.getNumberOfSources()));

		try {
			for (Source source = inputOriginal.getNext(); source != null; source = inputOriginal.getNext()) {
				assertSame(source, inputChanged.getNext());
			}
		} catch (ReachedEndException e) {
			// everything ok!!!
		}
		
	}

	private static void assertSame(final Source inputSource, final Source sourceToBeCompared) {
		assertThat(inputSource.toString(), is(sourceToBeCompared.toString()));
		assertThat(inputSource.getLanguage(), is(sourceToBeCompared.getLanguage()));
		assertThat(inputSource.getLocation(), is(sourceToBeCompared.getLocation()));
		assertThat(inputSource.getContent().toString(), is(sourceToBeCompared.getContent().toString()));
	}
}
