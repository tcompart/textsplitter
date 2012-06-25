package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	CopyControllerUnitTest.class,
	ConfiguratorUnitTest.class,
	QueueUnitTest.class,
	ThreadExecutorTest.class,
	ThreadInterruptingTest.class,
	PropertyUnitTest.class,
	TextfileLanguageFilterUnitTest.class,
	MappingFactoryUnitTest.class,
	TextfileParserUnitTest.class,
	SelectorPoolUnitTest.class,
	LocationUnitTest.class,
	SourceUnitTest.class,
	TextfileUnitTest.class,
	SourceInputstreamPoolUnitTest.class
})
public class AllUnitTests {

}
