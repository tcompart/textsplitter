/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import java.text.DecimalFormat;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class DecimalFormatUnitTest {

	@Test
	public void test() {
		
		assertThat(new DecimalFormat("0000").format(1), is("0001"));
	}
	
}
