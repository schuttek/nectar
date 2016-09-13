package org.nectarframework.base.tools;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Placeholder, not yet implemented!!
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
 * @author kai
 *
 */

public class DoubleFrameHashMap implements Map<K, V> {

	private Map<K, V> readMap;
	private Map<K, V> writeMap;

	@Override
	public int size() {
		return readMap.size();
	}

	@Override
	public boolean isEmpty() {
		return readMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
return readMap.isEmpty()	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public V get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V put(K key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<K> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
}
