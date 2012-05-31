/**
 * 
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Location;

public class LocationUnitTest {
	
	private static final String baseURL1 = "http://www.base.com/resource?type=index&other=attributes#test";

	private static URL url1;

	@BeforeClass
	public static void setUpClass() throws MalformedURLException {
		url1 = new URL(baseURL1);
	}

	
	@Test
	public void create() {
		Location location = new Location(url1);
		assertThat(location, notNullValue());
		assertThat(location.getURL(), is(url1));
		assertThat(location.getDomain(), is("com"));
	}
	
	
}
