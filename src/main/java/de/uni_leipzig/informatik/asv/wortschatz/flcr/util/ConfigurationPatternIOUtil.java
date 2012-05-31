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

public class ConfigurationPatternIOUtil {

	public static final String DELIMITER = " ";

	public static final Pattern pattern = Pattern.compile("([a-z0-9_]+)\\.([a-z]+)");

	public static Map<String, Set<String>> convert(File textfile) throws FileNotFoundException, IOException {

		if (textfile == null) { throw new NullPointerException(); }

		if (!textfile.exists()) { throw new FileNotFoundException(String.format(
				"The file located at '%s' does not exists and cannot be read.", textfile.getName())); }

		LinkedHashMap<String, Set<String>> result = new LinkedHashMap<String, Set<String>>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(textfile));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				for (String splitted : line.split(DELIMITER)) {
					Matcher matcher = pattern.matcher(splitted);
					if (splitted.length() > 0 && !splitted.isEmpty() && matcher.find()) {
						String language = matcher.group(1);
						String domain = matcher.group(2);
						assert language != null;
						assert !language.isEmpty();
						assert domain != null;
						assert !domain.isEmpty();
						if (!result.containsKey(language)) {
							result.put(language, new LinkedHashSet<String>());
						}
						result.get(language).add(domain);
					}
				}
			}
		} catch (IOException ex) {
			throw new IOException(ex);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return result;
	}

}
