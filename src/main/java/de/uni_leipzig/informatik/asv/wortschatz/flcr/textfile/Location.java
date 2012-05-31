/**
 * 
 */
package de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile;

import java.net.URL;

/**
 * @author <a href="mail:compart@informatik.uni-leipzig.de">Torsten Compart</a>
 * 
 */
public class Location {

	private URL url;

	public Location(final URL location) {
		if (location == null) { throw new NullPointerException(); }
		this.url = location;
	}

	public String getDomain() {
		String host = this.getURL().getHost();
		return host.substring(host.lastIndexOf(".") + 1);
	}

	public URL getURL() {
		return this.url;
	}
	
	@Override
	public int hashCode() {
		int hashCode = 0;
		
		final int hash = 27;
		final int seed = 11;
		
		hashCode += hash*seed + url.toString().hashCode();
		
		return hashCode;
	}
	
	@Override
	public boolean equals(final Object thatObj) {
		
		if (thatObj != null && thatObj instanceof Location) {
			Location that = (Location) thatObj;
			
			return that.url.equals(this.url);
			
		}
		
		
		return false;
	}
	
}
