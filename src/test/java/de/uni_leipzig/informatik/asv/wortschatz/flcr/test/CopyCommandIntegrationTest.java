package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFile;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.CopyCommand;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.MappingFactory;

public class CopyCommandIntegrationTest {

	private static final MappingFactory mappingFactory = new MappingFactory();
	
	private static TextFile textFile;
	private static Source source;
	private static File BIG_FILE;
	private static File mappedFile;

	@BeforeClass
	public static void setUpResources() throws IOException {
		BIG_FILE = new ClassPathResource("Unigramm/FL_spa0000.txt").getFile();
		textFile = new TextFile(BIG_FILE);
		source = textFile.getNext();
		mappedFile = mappingFactory.getSourceDomainMapping( textFile, source);
	}

	@Test
	public void create() {
		assertThat( textFile, notNullValue());
		assertThat(source, notNullValue());
	}

	@Test(timeout = 200)
	public void testTimeForTimeOut() {
		new CopyCommand(source, new File("outputfile"));
	}

	@Test(timeout = 100)
	public void testTimeForTimeOut2() {
		new CopyCommand(source, mappedFile);
	}

}
