package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ComplexCopyManagerUnitTest.class,
	ConfiguratorUnitTest.class,
	LocationUnitTest.class,
	MappingFactoryUnitTest.class,
	PropertyUnitTest.class,
	QueueUnitTest.class,
	SelectorPoolUnitTest.class,
	SourceInputstreamPoolUnitTest.class,
	SourceUnitTest.class,
	TextfileLanguageFilterUnitTest.class,
	TextfileParserUnitTest.class,
	TextfileUnitTest.class,
	ThreadExecutorTest.class,
	ThreadInterruptingTest.class
})
public class AllUnitTests {

}
