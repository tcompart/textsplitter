package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ConfiguratorUnitTest.class,
	QueueUnitTest.class,
	ThreadExecutorTest.class,
	ThreadInterruptingTest.class,
	PropertyUnitTest.class,
	TextfileParserUnitTest.class,
	SelectorPoolUnitTest.class,
	LocationUnitTest.class,
	SourceUnitTest.class,
	TextfileUnitTest.class,
	SourceInputstreamPoolUnitTest.class,
	CopyControllerUnitTest.class
})
public class AllUnitTests {

}
