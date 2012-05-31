package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import java.lang.reflect.Method;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.TestClass;

public class BasicUnitTest extends TestClass {

	private static final String CUT = new String("----------------------------------------");
	
	private static int methodCount = 0;
	
	public BasicUnitTest(Class<?> klass) {
		super(klass);
	}

	protected Calendar before = Calendar.getInstance();

	private Method currentMethod;
	
	@Before
	public void setUp() {
		currentMethod = this.getAnnotatedMethods(Test.class).get(methodCount).getMethod();
		methodCount += 1;
		System.out.println(CUT);
		System.out.println(String.format("Starting next method '%s' of test case '%s'", currentMethod.getName(), this.getClass().getSimpleName()));
		System.out.println(CUT);
	}

	@After
	public void tearDown() {
		long after = Calendar.getInstance().getTimeInMillis();
		
		final String TEARDOWN_MSG;
		final long timeItTook = after - this.before.getTimeInMillis();
		final String timeTookMsg = String.format("Run took: %d milliseconds.", (timeItTook));
		String timeOutMsg = "";
		if (this.currentMethod.isAnnotationPresent(Test.class)) {
			long timeout = this.currentMethod.getAnnotation(Test.class).timeout();			
			if (timeout > 0) {
				timeOutMsg = String.format("Expected timeout was '%d' milliseconds. That means: '%s'!", timeout, (timeout - timeItTook > 0) ? "SUCCESS" : "FAILED");
			}
		}
		TEARDOWN_MSG = String.format("%s %s", timeTookMsg, timeOutMsg);
		
		System.out.println(CUT);
		System.out.println(TEARDOWN_MSG);
		System.out.println(CUT);
	}

}
