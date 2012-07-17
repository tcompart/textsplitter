package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.CopyCommand;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public class CopyCommandIntegrationTest {

	private static final MappingFactory mappingFactory = new MappingFactory();
	
	private static Textfile textfile;
	private static Source source;
	private static File BIG_FILE;
	private static File mappedFile;

	@BeforeClass
	public static void setUpResources() throws IOException {
		BIG_FILE = new FileSystemResource("Unigramm/FL_spa0000.txt").getFile();
		textfile = new Textfile(BIG_FILE);
		source = textfile.getNext();
		mappedFile = mappingFactory.getSourceDomainMapping(textfile, source);
	}

	@Test
	public void create() {
		assertThat(textfile, notNullValue());
		assertThat(source, notNullValue());
	}

	@Test(timeout = 150)
	public void testTimeForTimeOut() {
		new CopyCommand(source, new File("outputfile"));
	}

	@Test(timeout = 100)
	public void testTimeForTimeOut2() {
		new CopyCommand(source, mappedFile);
	}

}
