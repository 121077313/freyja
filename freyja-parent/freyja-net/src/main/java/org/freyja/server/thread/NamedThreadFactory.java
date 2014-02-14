package org.freyja.server.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
	final AtomicInteger threadNumber = new AtomicInteger(1);

	final String name;

	public NamedThreadFactory(String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, name + "-thread-" + threadNumber.getAndIncrement());
	}
}
