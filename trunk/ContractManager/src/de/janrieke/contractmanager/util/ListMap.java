package de.janrieke.contractmanager.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListMap<K, V> extends HashMap<K, List<V>> {
	/**
	 * The UID.
	 */
	private static final long serialVersionUID = 8323975575855457935L;

	/**
	 * Adds the given value to the list of key.
	 * Will create the list if it does not exist yet.
	 * 
	 * @param key The key to whose list the value will be added 
	 * @param value The value to add to the list
	 */
	public void addToList(K key, V value) {
		if (this.containsKey(key)) {
			this.get(key).add(value);
		} else {
			List<V> list = new ArrayList<V>();
			list.add(value);
			this.put(key, list);
		}
	}

	/**
	 * Gets the list that corresponds to the given key.
	 * If create is true, a new list will be created and added
	 * to the map. If false, an empty list is returned.
	 * 
	 * @param key The key for which the list will be returned
	 * @param create Whether to add a new list for the given key if 
	 * non exists
	 * @return The list. Can never be null.
	 */
	public List<V> getList(K key, boolean create) {
		if (this.containsKey(key)) {
			return this.get(key);
		} else {
			List<V> list = new ArrayList<V>();
			if (create) {
				this.put(key, list);
			}
			return list;
		}
	}
	
	/**
	 * Computes a list of keys in whose lists the given value is. 
	 * 
	 * @param value the value to search for
	 * @return a list of all keys whose lists contain the value
	 */
	public List<K> getKeyFromListEntries(V value) {
		List<K> result = new ArrayList<K>(); 
		for (Map.Entry<K, List<V>> entry : this.entrySet()) {
			if (entry.getValue().contains(value))
				result.add(entry.getKey());
		}
		return result;
	}
}
