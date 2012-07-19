/**
 * 
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mail:compart@informatik.uni-leipzig.de">Torsten Compart</a>
 * 
 */
public class Source {

	public static final String locationStart = "<location>";
	public static final String locationEnd = "</location>";
	public static final String languageStart = "<language>";
	public static final String languageEnd = "</language>";

	private static Logger log = LoggerFactory.getLogger(Source.class);

	private final String source;
	private final StringBuffer content;

	public Source(final String sourceInputString, final StringBuffer inputContent) {

		if (sourceInputString == null || inputContent == null) {
			throw new NullPointerException();
		}
		if (sourceInputString.isEmpty() || sourceInputString.indexOf(locationStart) < 0 || sourceInputString.indexOf(languageStart) < 0) {
			throw new IllegalArgumentException(String.format("The assigned source string, seems to be not valid: '%s'. It has to be in the format: '%s'", sourceInputString, "<source><location>....</location>{<language>...</language>}"));
		}
		this.content = inputContent;
		this.source = sourceInputString;

	}

	private Location location;

	public Location getLocation() {

		if (this.location == null && this.source.indexOf(locationStart) > 0 && this.source.indexOf(locationEnd) > 0) {
			String locationSubString = this.source.substring(
					this.source.indexOf(locationStart) + locationStart.length(), this.source.indexOf(locationEnd));
			try {
				this.location = new Location(new URL(locationSubString));
			} catch (MalformedURLException ex) {
				log.warn("[{}]: Malformed URL exception because of not initialized location string: '{}'", this.toString(), locationSubString);
			}
			log.debug("Found location '{}' of source '{}'", locationSubString, this.source);
		}
		return this.location;
	}

	private String language;
	public String getLanguage() {
		if (this.language == null && this.source.indexOf(languageStart) > 0 && this.source.indexOf(languageEnd) > 0) {
			this.language = this.source.substring(
					this.source.indexOf(languageStart) + languageStart.length(), this.source.indexOf(languageEnd));
			if (this.language == null || this.language.isEmpty() || this.language.equals("null")) {
				this.language = Textfile.NO_LANGUAGE;
			}
			
			log.info("Found language '{}' of source '{}'", this.language, this.source);
		}
		return this.language;
	}

	public StringBuffer getContent() {
		return this.content;
	}

	@Override
	public String toString() {
		return this.source;
	}

	private int lineNumber = 0;

	public void setLineNumber(int inputLineNumber) {
		this.lineNumber = inputLineNumber;
	}
	
	public int getLineNumber() {
		return this.lineNumber;
	}

/*	@Override
	public int hashCode() {

		final int seed = 7;
		final int hash = 23;

		int hashCode = 0;

		hashCode += seed * hash + this.source.hashCode();
		hashCode += seed * hash + this.content.toString().hashCode();
		hashCode -= seed * hash + this.content.length();

		return hashCode;
	}

	@Override
	public boolean equals(Object that) {
		if (that != null && that instanceof Source) {
			Source thatSource = (Source) that;
			if (thatSource == this || (
					thatSource.source.equals(this.source) && 
					thatSource.content.toString().equals(this.content.toString()))) {
				return true;
			}
		}

		return false;

	}
*/
}
