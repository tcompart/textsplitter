/**
 * 
 */
package de.unileipzig.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author <a href="mail:grigull@informatik.uni-leipzig.de">Torsten Grigull</a>
 *
 */
public class FileChannelUnitTest {

	private File SOURCE;
	private File GOAL;
	
	@Before
	public void setUp() {
		
		SOURCE = new File("input.file");
		GOAL = new File("output.file");
		
		if (SOURCE.exists()) {
			SOURCE.delete();
		}
		
		if (GOAL.exists()) {
			GOAL.delete();
		}
		
	}
	
	@After
	public void tearDown() {
		SOURCE.delete();
		GOAL.delete();
	}
	
	@Ignore
	@Test
	public void testFileChannel1GB() throws IOException {
		
		long size = 1073741824;
		size = 1000;
		this.fillSourceFile(size);
		
		long that = System.currentTimeMillis();
		
		this.transferViaBufferedWriter();
		
		long now = System.currentTimeMillis();
		
		assertFileContent(GOAL, size);
		
		long then = System.currentTimeMillis();
		
		this.transferViaFileChannel();
		
		long after = System.currentTimeMillis();
		
		assertFileContent(GOAL, size);
		
		System.out.println((now - that)+" :vs: "+(after-then));
		assertThat(now - that > after - then, is(true));
		
	}
	
	@Ignore
	@Test
	public void testFileChannel5GB() throws IOException {
		
		long size = new Long("5368709120");
		size = 5000;
		this.fillSourceFile(size);
		
		long that = System.currentTimeMillis();
		
		this.transferViaBufferedWriter();
		
		long now = System.currentTimeMillis();
		
		assertFileContent(GOAL, size);
		
		long then = System.currentTimeMillis();
		
		this.transferViaFileChannel();
		
		long after = System.currentTimeMillis();
		
		assertFileContent(GOAL, size);
		
		assertThat((now - that)+" :vs: "+(after-then),now - that > after - then, is(true));
	}
	
	@Ignore
	@Test
	public void testFileChannel20GB() throws IOException {
		
//		long size = new Long("21474836480");
		long size = 20000;
		this.fillSourceFile(size);
		
		long that = System.currentTimeMillis();
		
		this.transferViaBufferedWriter();
		
		long now = System.currentTimeMillis();
		
		assertFileContent(GOAL, size);
		
		long then = System.currentTimeMillis();
		
		this.transferViaFileChannel();
		
		long after = System.currentTimeMillis();
		
		assertFileContent(GOAL, size);
		
		assertThat((now - that)+" :vs: "+(after-then),now - that > after - then, is(true));
		
	}
	
	public void assertFileContent(File output, long size) {
		
		assertThat(output.exists(), is(true));
		assertThat(output.length(), is(size)); 
		
		assertThat(SOURCE.exists(), is(true));
		assertThat(SOURCE.length(), is(size));
		
		GOAL.delete();
	}
	
	public void fillSourceFile(long size) throws IOException {
		
		if (SOURCE.exists()) {
			SOURCE.delete();
		}
		
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(SOURCE));
		
		Random random = new Random();
		for (long count = 0; count < size; count++) {
			out.write(0);
		}
		
		out.flush();
		out.close();
		
		assertThat(SOURCE.exists(), is(true));
		assertThat(SOURCE.length(), is(size));
		
	}
	
	public void transferViaBufferedWriter() throws IOException {
		
		InputStreamReader reader = new InputStreamReader(new FileInputStream(SOURCE));
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(GOAL));
		int line = -1;
		
		while((line = reader.read()) != -1) {
			writer.write(line);
		}
		
		writer.flush();
		writer.close();
		
		reader.close();
	}
	
	public void transferViaFileChannel() throws IOException {
		
		FileInputStream in = new FileInputStream(SOURCE);
		FileOutputStream out = new FileOutputStream(GOAL);
		
		FileChannel inChannel = in.getChannel();
		FileChannel outChannel = out.getChannel();
		
		inChannel.transferTo(0, inChannel.size(), outChannel);
		
		inChannel.close();
		outChannel.close();
		
		in.close();
		out.close();
		
	}
	
}
