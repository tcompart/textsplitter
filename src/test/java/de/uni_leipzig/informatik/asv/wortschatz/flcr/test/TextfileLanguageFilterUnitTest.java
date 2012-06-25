package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_leipzig.asv.clarin.common.tuple.Pair;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ConfigurationPatternIOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.TextfileLanguageFilter;

public class TextfileLanguageFilterUnitTest {

	private static File propertyFile = new File("/tmp/createdTextfileLanguageFilterList.txt");

	@BeforeClass
	public static void setUpClassPropertyFile() throws IOException {
		
		if (propertyFile.exists()) {
			propertyFile.delete();
		}
		
		FileWriter writer = new FileWriter(propertyFile);
		try {
			writer.write("deu.de");
			writer.write("\n");
			writer.write("deu.ch");
			writer.write("\n");
			writer.write("deu.at");
			writer.write("\n");
			writer.write("deu.be");
			writer.write("\n");
			writer.flush();
		} finally {
			writer.close();
		}
		
		assertThat(propertyFile.exists(), is(true));
		
		final Map<String,Set<String>> resultMap = ConfigurationPatternIOUtil.convert(propertyFile);
		
		assertThat(resultMap.toString(), resultMap.containsKey("deu"), is(true));
		assertThat(resultMap.toString(), resultMap.get("deu").contains("de"), is(true));
		assertThat(resultMap.toString(), resultMap.get("deu").contains("at"), is(true));
		assertThat(resultMap.toString(), resultMap.get("deu").contains("ch"), is(true));
		assertThat(resultMap.toString(), resultMap.get("deu").contains("be"), is(true));
		assertThat(resultMap.toString(), resultMap.get("deu").contains("en"), is(false));
		
	}

	@AfterClass
	public static void tearDownClassPropertyFile() {

		if (propertyFile.exists()) {
			propertyFile.delete();
		}
		
	}

	@Test
	public void create() {
		TextfileLanguageFilter filter = new TextfileLanguageFilter();

		assertThat(filter.apply(Pair.create("language", "unknown")), is(false));

		assertThat(filter, notNullValue());
	}

	@Test
	public void createWithParameter() {

		Properties properties = new Properties();
		properties.put(Configurator.PROPERTY_LANGUAGE_LIST_NAME, propertyFile.getAbsolutePath());
		Configurator configurator = new Configurator(properties);

		TextfileLanguageFilter filter = new TextfileLanguageFilter(configurator);

		assertThat(filter, notNullValue());
		assertThat(filter.apply(Pair.create("deu", "en")), is(false));
		assertThat(filter.apply(Pair.create("deu", "at")), is(true));
		assertThat(filter.apply(Pair.create("deu", "ch")), is(true));
		assertThat(filter.apply(Pair.create("deu", "de")), is(true));
		
		
	}

	@Test(expected = NullPointerException.class)
	public void createWithParameterNullPointerExpected() {
		TextfileLanguageFilter filter = new TextfileLanguageFilter(null);

		assertThat(filter, notNullValue());

	}

}
