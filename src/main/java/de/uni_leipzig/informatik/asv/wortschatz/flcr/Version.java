package de.uni_leipzig.informatik.asv.wortschatz.flcr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * User: torsten
 * Date: 2013/01
 * Time: 22:48
 *
 */
public class Version {
	//============================== CLASS VARIABLES ================================//
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( Version.class );

	private static final Version version = new Version( "version.properties" );

	//=============================== CLASS METHODS =================================//
	public static Version getVersion() {
		return version;
	}

	//===============================  VARIABLES ====================================//
	private final int version_major;
	private final int version_minor;
	private final int version_revision;
	private final String version_buildNr;

	//==============================  CONSTRUCTORS ==================================//
	private Version( final String propertyResourceName ) {
		final Properties properties = new Properties();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream( "version.properties" );
		try {
			properties.load( in );
		} catch ( IOException e ) {
			throw new RuntimeException( "Unable to locate resource '" + propertyResourceName + "'" );
		} finally {
			try {
				in.close();
			} catch ( IOException e ) {
				//todo	think about this
				assert false;
			}
		}
		version_major = Integer.parseInt( properties.getProperty( "version.major" ) );
		version_minor = Integer.parseInt( properties.getProperty( "version.minor" ) );
		version_revision = Integer.parseInt( properties.getProperty( "version.revision" ) );
		version_buildNr = properties.getProperty( "version.buildnr" );
	}

	public static String getVersionString() {
		StringBuilder sb = new StringBuilder();
		sb.append( version.version_major ).append( "." ).append( version.version_minor ).append( "." ).append( version.version_revision ).append( "-" ).append( version.version_buildNr );
		return sb.toString();
	}

	//=============================  PUBLIC METHODS =================================//
	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//
}