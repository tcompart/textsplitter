/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class IntegerLongTest {

	
	@Test
	public void test() {
		
		assertThat(((Integer) (1024*1024)) instanceof Integer, is(true));
		assertThat(1024*1024, is(1048576));
		assertThat((new Long(1024*1024)*new Long(1024)), is(new Long(1024*1048576)));
	}
	
}
