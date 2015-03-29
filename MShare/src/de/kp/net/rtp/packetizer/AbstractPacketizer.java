package de.kp.net.rtp.packetizer;

import java.io.IOException;
import java.io.InputStream;

abstract public class AbstractPacketizer extends Thread {

	protected InputStream fis;
	protected boolean running = false;

	public AbstractPacketizer() {
		super();
	}

	public AbstractPacketizer(Runnable runnable) {
		super(runnable);
	}

	public AbstractPacketizer(String threadName) {
		super(threadName);
	}

	public AbstractPacketizer(Runnable runnable, String threadName) {
		super(runnable, threadName);
	}

	public AbstractPacketizer(ThreadGroup group, Runnable runnable) {
		super(group, runnable);
	}

	public AbstractPacketizer(ThreadGroup group, String threadName) {
		super(group, threadName);
	}

	public AbstractPacketizer(ThreadGroup group, Runnable runnable, String threadName) {
		super(group, runnable, threadName);
	}

	public AbstractPacketizer(ThreadGroup group, Runnable runnable, String threadName, long stackSize) {
		super(group, runnable, threadName, stackSize);
	}

	public void startStreaming() {
		running = true;
		start();
	}

	public void stopStreaming() {
		try {
			fis.close();
		} catch (IOException e) {
			
		}
		running = false;
	}
	


}