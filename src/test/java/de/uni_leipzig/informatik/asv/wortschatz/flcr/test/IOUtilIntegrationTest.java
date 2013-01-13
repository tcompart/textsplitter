package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.IOUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 *
 * User: torsten
 * Date: 2012/09
 * Time: 23:49
 *
 */
public class IOUtilIntegrationTest {


	//============================== CLASS VARIABLES ================================//
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( IOUtilIntegrationTest.class );
	public static File directory;
	public static final int CONSTANT_I = 3;
	public static final int CONSTANT_J = 5;
	public static final int CONSTANT_K = 10;

	//===============================  VARIABLES ====================================//
	//==============================  CONSTRUCTORS ==================================//
	public IOUtilIntegrationTest() {
		log.debug( "Initialized default instance: %s", IOUtilIntegrationTest.class.getSimpleName() );
	}

	//=============================  PUBLIC METHODS =================================//
	@BeforeClass
	public static void createRecursivelyFiles() throws IOException {
		directory = new File( "testDirectoryTest"+System.currentTimeMillis() );
		directory.deleteOnExit();
		if ( directory.exists() || directory.mkdirs() ) {
			for ( int i = 0; i < CONSTANT_I; i++ ) {
				File newChildDirectory = new File( directory, "testDirectoryChildren" + i );
				newChildDirectory.deleteOnExit();
				if ( newChildDirectory.exists() || newChildDirectory.mkdirs() ) {
					for ( int j = 0; j < CONSTANT_J; j++ ) {
						File newChildChildDirectory = new File( newChildDirectory, "testDirectoryChildren" + j );
						newChildChildDirectory.deleteOnExit();
						if ( newChildChildDirectory.exists() || newChildChildDirectory.mkdirs() ) {
							for ( int k = 0; k < CONSTANT_K; k++ ) {
								File newFile = new File(newChildChildDirectory, "testFile"+i+j+k);
								newFile.deleteOnExit();
								assert newFile.exists() || newFile.createNewFile();
							}
						} else {
							throw new IOException();
						}
					}
				} else {
					throw new IOException();
				}
			}
		} else {
			throw new IOException(" The directory seems to exist already");
		}
	}

	@AfterClass
	public static void tearDownRecursively() {
		IOUtil.removeDirectory(directory);
	}

	@Test
	public void assertStartUp() {
		assertThat(directory.exists(), is(true));
		assertThat(directory.isDirectory(), is(true));
		assertThat(directory.list().length > 0, is(true));
	}

	@Test
	public void integrateThroughTheFiles() {

		Collection<File> files = IOUtil.getFiles(directory, true);
		assertThat(files.size(), is(CONSTANT_I*CONSTANT_J*CONSTANT_K));

	}


	//======================  PROTECTED/PACKAGE METHODS =============================//
	//============================  PRIVATE METHODS =================================//
	//=============================  INNER CLASSES ==================================//

}
