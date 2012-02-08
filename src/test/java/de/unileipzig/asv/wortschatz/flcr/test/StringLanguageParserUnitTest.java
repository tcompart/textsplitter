/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.unileipzig.asv.wortschatz.flcr.util.StringLanguageParser;
import de.unileipzig.asv.wortschatz.flcr.util.StringLanguageParser.LanguageNotFoundException;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class StringLanguageParserUnitTest {

	private StringLanguageParser language;

	@Before
	public void setUp() {
		language = new StringLanguageParser();
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void testString() {
		
		final String prefix = "FL_";
		final String suffix = "0000.txt";
		
		final String NAME = "srp_latn";
		
		language.setPrefix(prefix);
		language.setSuffix(suffix);
		
		assertThat(language.updateName(prefix+NAME+suffix), is(NAME));
		
		
	}
	
	@Test
	public void testWithoutPrefix() {
		
		final String prefix = null;
		final String suffix = "0000.txt";
		
		final String NAME = "srp_latn";
		
		language.setSuffix(suffix);
		
		assertThat(language.updateName(NAME+suffix), is(NAME));
	}
	
	@Test
	public void testWithoutSuffix() {
		
		final String prefix = "FL_";
		
		final String NAME = "srp_latn";
		
		language.setPrefix(prefix);
		
		assertThat(language.updateName(prefix+NAME), is(NAME));
	}
}
