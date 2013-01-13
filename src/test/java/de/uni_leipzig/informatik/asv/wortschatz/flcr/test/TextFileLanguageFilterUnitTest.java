package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ConfigurationPatternIOUtil;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Pair;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.TextFileLanguageFilter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TextFileLanguageFilterUnitTest {

	private static File propertyFile = new File( "/tmp/createdTextFileLanguageFilterList.txt" );

	@BeforeClass
	public static void setUpClassPropertyFile() throws IOException {
		if ( propertyFile.exists() && !propertyFile.delete() ) {
			propertyFile.deleteOnExit();
		}
		FileWriter writer = new FileWriter( propertyFile );
		try {
			writer.write( "deu.de" );
			writer.write( "\n" );
			writer.write( "deu.ch" );
			writer.write( "\n" );
			writer.write( "deu.at" );
			writer.write( "\n" );
			writer.write( "deu.be" );
			writer.write( "\n" );
			writer.flush();
		} finally {
			writer.close();
		}

		assertThat( propertyFile.exists(), is( true ) );

		final Map<String, Set<String>> resultMap = ConfigurationPatternIOUtil.convert( propertyFile );

		assertThat( resultMap.toString(), resultMap.containsKey( "deu" ), is( true ) );
		assertThat( resultMap.toString(), resultMap.get( "deu" ).contains( "de" ), is( true ) );
		assertThat( resultMap.toString(), resultMap.get( "deu" ).contains( "at" ), is( true ) );
		assertThat( resultMap.toString(), resultMap.get( "deu" ).contains( "ch" ), is( true ) );
		assertThat( resultMap.toString(), resultMap.get( "deu" ).contains( "be" ), is( true ) );
		assertThat( resultMap.toString(), resultMap.get( "deu" ).contains( "en" ), is( false ) );
	}

	@AfterClass
	public static void tearDownClassPropertyFile() {
		if ( propertyFile.exists() && !propertyFile.delete() ) {
			propertyFile.deleteOnExit();
		}
	}

	@Test
	public void create() {
		TextFileLanguageFilter filter = new TextFileLanguageFilter();
		assertThat( filter.apply( new Pair<String, String>( "language", "unknown" ) ), is( false ) );
		assertThat( filter, notNullValue() );
	}

	@Test
	public void createWithParameter() {

		final Properties properties = new Properties();
		properties.put( Configurator.PROPERTY_LANGUAGE_LIST_NAME, propertyFile.getAbsolutePath() );

		final Configurator configurator = new Configurator( properties );

		final TextFileLanguageFilter filter = new TextFileLanguageFilter( configurator );

		assertThat( filter, notNullValue() );
		assertThat( filter.apply( new Pair<String, String>( "deu", "en" ) ), is( false ) );
		assertThat( filter.apply( new Pair<String, String>( "deu", "at" ) ), is( true ) );
		assertThat( filter.apply( new Pair<String, String>( "deu", "ch" ) ), is( true ) );
		assertThat( filter.apply( new Pair<String, String>( "deu", "de" ) ), is( true ) );


	}

	@Test( expected = NullPointerException.class )
	public void createWithParameterNullPointerExpected() {
		final TextFileLanguageFilter filter = new TextFileLanguageFilter( null );
		assertThat( filter, notNullValue() );
	}

}
