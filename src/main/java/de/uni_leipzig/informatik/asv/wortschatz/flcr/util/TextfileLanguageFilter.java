/**
 * 
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.google.common.base.Predicate;

import de.uni_leipzig.asv.clarin.common.tuple.Pair;

public class TextfileLanguageFilter implements Predicate<Pair<String, String>> {

	private static final Logger log = LoggerFactory.getLogger(TextfileLanguageFilter.class);
	
	private final Map<String, Set<String>> languageList;

	private final String instance_configuration_file_name;
	
	public TextfileLanguageFilter() {
		this(Configurator.getConfiguration());
	}

	public TextfileLanguageFilter(final Configurator inputConfiguration) {
		final Resource resourceFile = new FileSystemResource(inputConfiguration.getTextfileLanguageListFileName());
		this.instance_configuration_file_name = resourceFile.getFilename();
		Map<String, Set<String>> temporaryMap = null;
		try {
			final File languageFile = resourceFile.getFile();
			temporaryMap = ConfigurationPatternIOUtil.convert(languageFile);
			log.debug(String.format("%s: The language list was filled with %d language %s and a number of linked domains.", this.instance_configuration_file_name, temporaryMap.size(), (temporaryMap.size() == 1 ? "entry" : "entries")));
		} catch (IOException ex) {
			log.warn(String.format("%s: The language list linked with the domains does not exist. Will use an empty white list instead.", this.instance_configuration_file_name));
			temporaryMap = Collections.emptyMap();
		}
		
		this.languageList = Collections.unmodifiableMap(temporaryMap);
	}

	@Override
	public boolean apply(final Pair<String, String> pairLanguageDomain) {
		final Set<String> domains = this.languageList.get(pairLanguageDomain.first());

		return domains != null && domains.contains(pairLanguageDomain.second());
	}

}
