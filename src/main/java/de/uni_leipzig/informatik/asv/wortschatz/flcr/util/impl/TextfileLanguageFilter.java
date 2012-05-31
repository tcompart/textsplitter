/**
 * 
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.util.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import de.uni_leipzig.asv.clarin.common.tuple.Pair;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Configurator;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.Filter;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.ConfigurationPatternIOUtil;

/**
 * @author <a href="mail:compart@informatik.uni-leipzig.de">Torsten Compart</a>
 * 
 */
public class TextfileLanguageFilter implements Filter<Pair<Textfile, String>> {

	private final Logger log = LoggerFactory.getLogger("TextfileLanguageFilter:AnonymousInnerClass:Filter<Textfile>");
	private final Map<String, Set<String>> languageList;

	public TextfileLanguageFilter() {
		this(Configurator.getConfiguration());
	}

	public TextfileLanguageFilter(Configurator inputConfiguration) {
		final Resource resourceFile = new FileSystemResource(inputConfiguration.getTextfileLanguageListFileName());
		Map<String, Set<String>> temporaryMap = null;
		try {
			final File languageFile = resourceFile.getFile();
			temporaryMap = ConfigurationPatternIOUtil.convert(languageFile);
		} catch (IOException ex) {
			this.log
					.warn("The language list linked with the domains does not exist. Will use an empty white list instead.");
			temporaryMap = Collections.emptyMap();
		}
		this.languageList = Collections.unmodifiableMap(temporaryMap);
	}

	@Override
	public boolean apply(final Pair<Textfile, String> pairLanguageDomain) {
		final Set<String> domains = this.languageList.get(pairLanguageDomain.first().getLanguage());

		return domains != null && domains.contains(pairLanguageDomain.second());
	}

}
