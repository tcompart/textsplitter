package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ReserverUtil;

public class ReserverUtilIntegrationTest {
	
	private File file;

	@Before
	public void setUp() {
		file = new File("reserverutil.test");
	}

	@Test
	public void blockingTest() throws InterruptedException {
		
		
		assertThat(ReserverUtil.reserve(file).isJust(), is(true));
		assertThat(ReserverUtil.reserve(new File("something else")).isJust(), is(true));
		assertThat(ReserverUtil.reserve(file).isNothing(), is(true));
		
	}
	
}
