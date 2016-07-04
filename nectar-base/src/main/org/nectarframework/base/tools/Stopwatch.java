package org.nectarframework.base.tools;

import java.util.LinkedList;

public class Stopwatch {

	private class Mark {
		public String tag;
		public long time;
	}

	private long startTime;
	private LinkedList<Mark> marks = new LinkedList<Mark>();
	private long endTime;

	public Stopwatch() {
		start();
	}

	public void reset() {
		startTime = 0;
		marks.clear();
		endTime = 0;
	}

	public void start() {
		reset();
		startTime = System.nanoTime();
	}

	public void mark() {
		mark("Mark");
	}

	public void mark(String tag) {
		Mark m = new Mark();
		m.tag = tag;
		m.time = System.nanoTime();
		marks.add(m);
	}

	public void stop() {
		endTime = System.nanoTime();
	}

	public long sinceStart() {
		if (endTime == 0) {
			return System.nanoTime() - startTime;
		} else {
			return endTime - startTime;
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Stopwatch: ");
		long et = (endTime == 0) ? System.nanoTime() : endTime;
		sb.append("total: " + timeDiffToString(et - startTime));
		long lastMark = startTime;
		for (Mark m : marks) {
			sb.append(" . " + m.tag + ": " + timeDiffToString(m.time - lastMark));
			lastMark = m.time;
		}
		return sb.toString();
	}

	private String timeDiffToString(long t) {
		long days = t / (24 * 60 * 60 * 1000 * 1000 * 1000);
		t -= days * 24 * 60 * 60 * 1000 * 1000 * 1000;
		long hours = t / (60 * 60 * 1000 * 1000 * 1000);
		t -= hours * 60 * 60 * 1000 * 1000 * 1000;
		long minutes = t / (60 * 1000 * 1000 * 1000);
		t -= minutes * 60 * 1000 * 1000 * 1000;
		long seconds = t / (1000 * 1000 * 1000);
		t -= seconds * 1000 * 1000 * 1000;
		long ms = t / (1000 * 1000);
		t -= ms * 1000 * 1000;
		long us = t / 1000;
		t -= us * 1000;
		long ns = t;

		StringBuffer sb = new StringBuffer();
		boolean trip = false;
		if (days > 0) {
			sb.append(days + "d ");
			trip = true;
		}
		if (hours > 0 || trip) {
			if (hours < 10) {
				sb.append("0");
			}
			sb.append(hours + "h ");
		}
		if (minutes > 0 || trip) {
			if (minutes < 10) {
				sb.append("0");
			}
			sb.append(minutes + "M ");
		}
		if (seconds > 0 || trip) {
			if (seconds < 10) {
				sb.append("0");
			}
			sb.append(seconds + "s ");
		}
		if (ms < 100) {
			sb.append("0");
		}
		if (ms < 10) {
			sb.append("0");
		}
		sb.append(ms + "m");

		if (us < 100) {
			sb.append("0");
		}
		if (us < 10) {
			sb.append("0");
		}
		sb.append(us + "u");

		if (ns < 100) {
			sb.append("0");
		}
		if (ns < 10) {
			sb.append("0");
		}
		sb.append(ns + "n");

		return sb.toString();
	}

	public static void main(String[] args) {

		Stopwatch sw = new Stopwatch();
		synchronized (sw) {
			sw.start();
			try {

				sw.wait(100);

				sw.mark("blah");

				sw.wait(185);
				sw.stop();
				
				System.out.println(sw.toString());
				
				sw.start();
				
				System.out.println(sw.toString());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
