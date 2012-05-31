package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InstanceNamePatternFactory {

	private static final HashMap<Class<?>, AtomicInteger> map = new HashMap<Class<?>, AtomicInteger>();

	private static String PATTERN;

	public static String getFactoryPattern() {
		return PATTERN;
	}

	public static void setFactoryPattern(final String inputPattern) {
		PATTERN = inputPattern;
	}

	public static String getInstanceName(Class<?> inputClass) {
		if (!map.containsKey(inputClass)) {
			map.put(inputClass, new AtomicInteger(0));
		}
		final int number = map.get(inputClass).incrementAndGet();
		final String className = inputClass.getSimpleName();
		if (PATTERN == null) {
			PATTERN = "%s_%d";
		}
		return String.format(PATTERN, className, number);
	}

}
