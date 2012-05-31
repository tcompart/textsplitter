package de.uni_leipzig.informatik.asv.wortschatz.flcr.util.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Filter;

/*
 *
 *	configurable source filter
 *by the following file sourceLocationPattern:
 *
 *	LANGUAGE_DOMAIN_PREFIX.LANGUAGE.DOMAIN={ignore|accept}
 *
 *	default: accept all languages and all domains -> LANGUAGE_DOMAIN_PREFIX.*.*=accept
 *	special case: ignore for all languages domain .com -> LANGUAGE_DOMAIN_PREFIX.*.com=ignore
 *	special case: ignore all languages and all domains except for language 'spa':
 *		LANGUAGE_DOMAIN_PREFIX.*.*=ignore
 *		LANGUAGE_DOMAIN_PREFIX.spa.*=accept
 */
public class SourceFilter implements Filter<String> {

	public final static String LANGUAGE_DOMAIN_PREFIX = Configurator.PROPERTY_LANGUAGE_DOMAIN_PREFIX;
	
	private static final Pattern language_domain_pattern = Pattern.compile("^"+LANGUAGE_DOMAIN_PREFIX.replace(".", "\\.")+"\\.([a-z]+|\\*)\\.([a-z]{2,4}|\\*)$");
	
	private final Boolean SAME_BASE_LANGUAGE_SUFFICE = Configurator.getConfiguration().isGeneralLanguagePatternAllowed();
	
	private Map<String, Set<String>> allowedDomains;

	
	
	public SourceFilter() {
		
	}
	
	public SourceFilter(HashMap<String, Set<String>> inputAllowedDomains) {
		this.allowedDomains = inputAllowedDomains;
	}
	
	public static HashMap<String, Set<String>> filterForLanguageDomains(File file) throws FileNotFoundException {
		
		if (!file.exists() || !file.isFile()) { // provoke a null pointer exception at this point...
			throw new FileNotFoundException(String.format("File '%s' does not exist, or follows a wrong format.", file.toString()));
		}
		
		HashMap<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String propertyKey : properties.stringPropertyNames()) {
			Matcher matcher = language_domain_pattern.matcher(propertyKey);
			if (matcher.matches()) {
				String language = matcher.group(1);
				String domain = matcher.group(2);
				
				assert language != null; // cannot even be possible, because the sourceLocationPattern should not allow this
				assert domain != null; // cannot even be possible, because the sourceLocationPattern should not allow this
				
				
			}
		}
		
		
		return null;		
	}

	@Override
	public boolean apply(String input) {
		// TODO Auto-generated method stub
		return false;
	}

}
