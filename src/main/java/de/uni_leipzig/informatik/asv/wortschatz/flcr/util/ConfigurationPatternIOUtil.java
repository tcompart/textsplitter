package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.informatik.asv.wortschatz.flcr._development.Development;

public class ConfigurationPatternIOUtil {
	
	public static final String DELIMITER = " ";

	public static final Pattern pattern = Pattern.compile("([a-z0-9_]+)\\.([a-z]+)");

	private static final Logger log = LoggerFactory.getLogger(ConfigurationPatternIOUtil.class);
	
	
	public static Map<String, Set<String>> convert(File textfile) throws FileNotFoundException, IOException {

		if (textfile == null) { throw new NullPointerException(); }

		if (!textfile.exists()) { throw new FileNotFoundException(String.format("The file located at '%s' does not exists and cannot be read.", textfile.getName())); }

		LinkedHashMap<String, Set<String>> result = new LinkedHashMap<String, Set<String>>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(textfile));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				for (String splitted : line.split(DELIMITER)) {
					final String trimmedString = splitted.trim();
					
					if (Development.assertionsEvaluated) {
						assert trimmedString != null;
						assert !trimmedString.isEmpty();
					}
					
					Matcher matcher = pattern.matcher(trimmedString);
					
					if (matcher.matches()) {
						String language = matcher.group(1);
						String domain = matcher.group(2);

						if (!result.containsKey(language)) {
							result.put(language, new LinkedHashSet<String>());
						}
						
						result.get(language).add(domain);
						if (log.isDebugEnabled()) {
							log.debug(String.format("Language '%s' was linked with domain '%s'. Therefore the assigned language with domain will be filtered (previous entry was '%s').", language, domain, trimmedString));
						}

						if (Development.assertionsEvaluated) {
							assert language != null;
							assert !language.isEmpty();
							assert domain != null;
							assert !domain.isEmpty();
						}
						
					} else if (log.isWarnEnabled()) {
						log.warn(String.format("String '%s' did not match the pattern '%s'. If this wrong. Please change the content of the property file, or fix the implementation.", trimmedString, pattern.toString()));
					}
				}
			}
		} catch (IOException ex) {
			if (ex instanceof IOException)
				throw (IOException) ex;
			throw new IOException(ex);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return result;
	}

}
