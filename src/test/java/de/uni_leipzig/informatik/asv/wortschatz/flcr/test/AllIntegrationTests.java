package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	IntegrationTestCase.class,
	CopyControllerIntegrationTest.class,
	SelectorPoolIntegrationTest.class,
	TextfileIntegrationTest.class,
	SourceInputstreamPoolIntegrationTest.class
})
public class AllIntegrationTests {

}
