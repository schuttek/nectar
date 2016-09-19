package org.nectarframework.base.tools;

import java.util.Collection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * This map is intended to be both thread safe, and have (mostly) non mutex'd
 * reads.
 * 
 * HOWEVER, if you insert something into this map, and immediately try to read
 * the same key from the map, it probably won't give you the result you expect.
 * 
 * The idea is that this map is in fact 2 maps, one that handles writes, the
 * other reads, and every so often the two maps switch places.
 * 
 * As a result, this map will be eventually consistent, and while writes are
 * still synchronized, reads are not.
 * 
 * This map can be very effective if handling a massive number of reads per unit
 * time vs a small number of writes per unit time, especially in a massively
 * multithreaded use case.
 * 
 * This class isn't such a good idea because it's possible that between
 * readAllowed.get() and readCounter.increment(), the flip() happens,
 * potentially sending one or more threads on the Map that flip() is about to
 * update. The solution would be an
 * AtomicInteger.compareGreaterThanAndIncrement(), but that doesn't exist.
 * 
 * 
 * @author schuttek
 *
 */

public class DoubleBufferHashMap<K, V> implements Map<K, V> {
	
	private Map<K, V> readMap = new HashMap<>();
	private Map<K, V> writeMap = new HashMap<>();
	private LinkedList<Triple<Operation, Object, V>> operationList = new LinkedList<>();

	private AtomicBoolean readAllowed = new AtomicBoolean(true);
	private AtomicInteger readCounter = new AtomicInteger(0);

	private long lastFlipTime = System.currentTimeMillis();
	private long flipTimer = 3000; // 3 seconds

	private enum Operation {
		Put, Delete;
	}

	@Override
	public int size() {
		startRead();
		RuntimeException rethrow = null;
		int n = 0;
		try {
			n = readMap.size();
		} catch (RuntimeException t) {
			rethrow = t;
		}
		endRead();
		if (rethrow != null) {
			throw rethrow;
		}
		return n;
	}

	@Override
	public boolean isEmpty() {
		startRead();
		RuntimeException rethrow = null;
		boolean b = false;
		try {
			b = readMap.isEmpty();
		} catch (RuntimeException t) {
			rethrow = t;
		}
		endRead();
		if (rethrow != null) {
			throw rethrow;
		}
		return b;
	}

	@Override
	public boolean containsKey(Object key) {
		startRead();
		RuntimeException rethrow = null;
		boolean b = false;
		try {
			b = readMap.containsKey(key);
		} catch (RuntimeException t) {
			rethrow = t;
		}
		endRead();
		if (rethrow != null) {
			throw rethrow;
		}
		return b;
	}

	@Override
	public boolean containsValue(Object value) {
		startRead();
		RuntimeException rethrow = null;
		boolean b = false;
		try {
			b = readMap.containsValue(value);
		} catch (RuntimeException t) {
			rethrow = t;
		}
		endRead();
		if (rethrow != null) {
			throw rethrow;
		}
		return b;
	}

	@Override
	public V get(Object key) {
		startRead();
		RuntimeException rethrow = null;
		V v = null;
		try {
			v = readMap.get(key);
		} catch (RuntimeException t) {
			rethrow = t;
		}
		endRead();
		if (rethrow != null) {
			throw rethrow;
		}
		return v;
	}

	@Override
	public synchronized V put(K key, V value) {
		operationList.add(new Triple<>(Operation.Put, key, value));
		writeMap.put(key, value);
		return value;
	}

	@Override
	public synchronized V remove(Object key) {
		// Not entirely sure if we should return the value from the read map or
		// the write map...
		operationList.add(new Triple<>(Operation.Delete, key, null));
		V v = writeMap.remove(key);
		endRead();
		return v;
	}

	@Override
	public synchronized void putAll(Map<? extends K, ? extends V> m) {
		for (K k : m.keySet()) {
			V v = m.get(k);
			operationList.add(new Triple<>(Operation.Put, k, v));
			writeMap.put(k, v);
		}
		checkFlipTimer();
	}

	@Override
	public synchronized void clear() {
		writeMap.clear();
		checkFlipTimer();
	}

	@Override
	public Set<K> keySet() {
		startRead();
		RuntimeException rethrow = null;
		Set<K> sk = null;
		try {
			sk = readMap.keySet();
		} catch (RuntimeException t) {
			rethrow = t;
		}
		endRead();
		if (rethrow != null) {
			throw rethrow;
		}
		return sk;
	}

	@Override
	public Collection<V> values() {
		startRead();
		RuntimeException rethrow = null;
		Collection<V> cv = null;
		try {
			cv = readMap.values();
		} catch (RuntimeException t) {
			rethrow = t;
		}
		endRead();
		if (rethrow != null) {
			throw rethrow;
		}
		return cv;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		startRead();
		RuntimeException rethrow = null;
		Set<java.util.Map.Entry<K, V>> se = null;
		try {
			se = readMap.entrySet();
		} catch (RuntimeException t) {
			rethrow = t;
		}
		endRead();
		if (rethrow != null) {
			throw rethrow;
		}
		endRead();
		return se;
	}

	private void checkFlipTimer() {
		long now = System.currentTimeMillis();
		if (this.flipTimer > 0 && now > this.lastFlipTime + this.flipTimer) {
			flip();
			this.lastFlipTime = now;
		}
	}

	/**
	 * Flips the two maps, and updates the map that was being read from to the
	 * latest state.
	 */
	@SuppressWarnings("unchecked")
	private synchronized void flip() {
		readAllowed.set(false);
		while (readCounter.get() != 0) {
			Thread.yield();
		}

		Map<K, V> temp = readMap;
		readMap = writeMap;
		writeMap = temp;

		readAllowed.set(true);
		this.notifyAll();

		for (Triple<Operation, Object, V> t : operationList) {
			switch (t.getLeft()) {
			case Delete:
				writeMap.remove(t.getMiddle());
				break;
			case Put:
				writeMap.put((K) t.getMiddle(), t.getRight());
				break;
			}
		}
	}

	private void startRead() {
		if (!readAllowed.get()) {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
		}
		readCounter.incrementAndGet();
	}

	private void endRead() {
		readCounter.decrementAndGet();
	}

}
