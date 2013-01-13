package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	CopyCommandIntegrationTest.class,
	ComplexCopyManagerIntegrationTest.class,
	IntegrationTestCase.class,
	MappingFactoryIntegrationTest.class,
	ReserverUtilIntegrationTest.class,
	SelectorPoolIntegrationTest.class,
//	SimpleCopyManagerIntegrationTest.class,
	SourceInputstreamPoolIntegrationTest.class,
	TextFileIntegrationTest.class,
	ViewControllerIntegrationTest.class
})
public class AllIntegrationTests {

}
