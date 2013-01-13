/**
 *
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class TextFileLanguageFilter implements Predicate<Pair<String, String>> {

	private static final Logger log = LoggerFactory.getLogger( TextFileLanguageFilter.class );

	private final Map<String, Set<String>> languageList;

	public TextFileLanguageFilter() {
		this( Configurator.getGlobalConfiguration() );
	}

	public TextFileLanguageFilter( final Configurator inputConfiguration ) {
		final Resource resourceFile = new FileSystemResource( inputConfiguration.getTextFileLanguageListFileName() );
		final String instance_configuration_file_name = resourceFile.getFilename();

		Map<String, Set<String>> temporaryMap;
		try {
			final File languageFile = resourceFile.getFile();
			temporaryMap = ConfigurationPatternIOUtil.convert( languageFile );
			log.debug( String.format( "%s: The language list was filled with %d language %s and a number of linked domains.", instance_configuration_file_name, temporaryMap.size(), ( temporaryMap.size() == 1 ? "entry" : "entries" ) ) );
		} catch ( IOException ex ) {
			log.warn( String.format( "%s: The language list linked with the domains does not exist. Will use an empty white list instead.", instance_configuration_file_name ) );
			temporaryMap = Collections.emptyMap();
		}

		this.languageList = Collections.unmodifiableMap( temporaryMap );
	}

	@Override
	public boolean apply( final Pair<String, String> pairLanguageDomain ) {
		final Set<String> domains = this.languageList.get( pairLanguageDomain.getFirst() );

		return domains != null && domains.contains( pairLanguageDomain.getSecond() );
	}

}
