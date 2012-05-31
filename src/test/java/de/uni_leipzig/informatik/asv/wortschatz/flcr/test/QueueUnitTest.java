package de.uni_leipzig.informatik.asv.wortschatz.flcr.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class QueueUnitTest {

	private Collection<Object> queue;
	
	@Test
	public void testBlockingQueueForDuplicates() {
		queue = new LinkedList<Object>();
		assertDuplicates(queue, true);
		
	}
	
	@Test
	public void testLinkedHashSet() {
		queue = new LinkedHashSet<Object>();
		assertDuplicates(queue, false);
		
	}
	
	@Test
	public void testPriorityBlockingQueue() {
		queue = new PriorityBlockingQueue<Object>();
		
		assertDuplicates(queue,true);
		
	}

	private void assertDuplicates(Collection<Object> queue, final boolean duplicatesAllowed) {
		final String obj1 = new String("value1");
		
		assertThat(queue.size(), is(0));
		
		queue.add(obj1);
		
		assertThat(queue.size(), is(1));
		
		queue.add(obj1);
		
		if (duplicatesAllowed)
			assertThat(queue.size(), is(2));
		else
			assertThat(queue.size(), is(1));
		
	}
	
	@Test
	public void assertCapacity() throws InterruptedException {
		
		final int CAPACITY = 10;
		
		BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(CAPACITY);
		final AtomicInteger count = new AtomicInteger(0);
		final String value = "some value";
		final String pattern = "%s_%d";
		
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 1
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 2
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 3
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 4
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 5
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 6
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 7
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 8
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 9
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 10
		queue.offer(String.format(pattern, value, count.incrementAndGet())); // 11
		
		for (int i = 0; queue.poll() != null; i++) {
			assertThat(String.valueOf(i), i < CAPACITY, is(true));
		}
	}
	
}
