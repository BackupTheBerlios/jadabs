package ch.ethz.iks.evolution.adapter;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.cop.AdapterClassLoader;
import ch.ethz.iks.proxy.IProxy;
import ch.ethz.iks.proxy.cop.ProxyLoader;


/**
 * 
 * Need a custom HashMap to map proxy objects:
 * java.util.HashMap calls equals() on the key argument in the get() method
 * This would result in an endless recursion because this call would be forwarded to the prox's handler
 * that would try to invoke the method equals() on the original object. To do this, 
 * it would have to map the proxy object ot its original first by calling HashMap.get again (and again and again...)
 * This implementation makes use of the same() and hash() methods instead of the redirecting equals() and hashValue().
 * 
 * added possibility to register and get dynamic Proxy as keys, but is just minamally supported (put/get)
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */

 class ProxyRegistry implements Map, Cloneable, java.io.Serializable {

	private Hashtable dynProxyMap;

	/**
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public ProxyRegistry(int initialCapacity, float loadFactor) {
		//super(initialCapacity, loadFactor);
		if (initialCapacity < 0)
		throw new IllegalArgumentException("Illegal Initial Capacity: "+
											   initialCapacity);
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal Load factor: "+
											   loadFactor);
		if (initialCapacity==0)
			initialCapacity = 1;
		this.loadFactor = loadFactor;
		this.table = new Proxy2Hidden[initialCapacity];
		this.threshold = (int)(initialCapacity * loadFactor);
		
		dynProxyMap = new Hashtable(initialCapacity, loadFactor);
	}

	/**
	 * @param initialCapacity
	 */
	public ProxyRegistry(int initialCapacity) {
		//super(initialCapacity);
		this(initialCapacity, 0.70f);
	}

	/**
	 * 
	 */
	public ProxyRegistry() {
		//super();
		this(50, 0.70f);
	}

	/**
	 * @param t
	 */
	public ProxyRegistry(Map t) {
		//super(t);
		this(Math.max(2*t.size(), 22), 0.75f);
		Iterator entries = t.entrySet().iterator();
		while (entries.hasNext()) {
			Proxy2Hidden entry =  (Proxy2Hidden) entries.next();
			Object key = entry.getKey(); 
			
			this.put(key, entry.getValue());
			
		}
		
	}

	public synchronized Object getHiddenBy(IProxy proxy) {
		Proxy2Hidden tab[] = table;

		if (proxy != null) {
			int hash = proxy.hash();
			int index = (hash & 0x7FFFFFFF) % tab.length;
			for (Proxy2Hidden e = tab[index]; e != null; e = e.next)
				if ((e.hash == hash) && proxy.same((IProxy)e.key))
					return e.value;
		} else {
			for (Proxy2Hidden e = tab[0]; e != null; e = e.next)
				if (e.key == null)
					return e.value;
		}
		if (proxy != null) System.out.println(" proxies.get ERROR: original not found for proxy of "+proxy.dump());
		return null;
	}

	public Object get(Object key) {
		try{
			return getHiddenBy((IProxy) key);
		}
		catch (ClassCastException c) {
			//System.err.println(" classcast, key is not an IProxy");
			synchronized(this.dynProxyMap) {
				return this.dynProxyMap.get(key);
			}
		}
			
			 //throw new RuntimeException("NOT YET IMPLEMENTED: +ProxyRegistry.get");
			 //return false;
	}
	
	public synchronized Object getHiddenBy(Proxy dynamicProxy) {
		return dynProxyMap.get(dynamicProxy);
	}

	public Object put(Object key, Object hidden) {
		try {
			return register((IProxy) key, hidden);
		} catch (ClassCastException c) {
			return register((Proxy)key, hidden);
		}
	}
	
	public synchronized Object register(Proxy dynProxy, Object hidden) {
		return dynProxyMap.put(dynProxy, hidden);
	} 

	/**
	 * The hash table data.
	 */
	private transient Proxy2Hidden table[];

	/**
	 * The total number of mappings in the hash table.
	 */
	private transient int count;

	/**
	 * The table is rehashed when its size exceeds this threshold.  (The
	 * value of this field is (int)(capacity * loadFactor).)
	 *
	 * @serial
	 */
	private int threshold;

	/**
	 * The load factor for the hashtable.
	 *
	 * @serial
	 */
	private float loadFactor;

	/**
	 * The number of times this HashMap has been structurally modified
	 * Structural modifications are those that change the number of mappings in
	 * the HashMap or otherwise modify its internal structure (e.g.,
	 * rehash).  This field is used to make iterators on Collection-views of
	 * the HashMap fail-fast.  (See ConcurrentModificationException).
	 */
	private transient int modCount = 0;

	/**
	 * Returns the number of key-value mappings in this map.
	 *
	 * @return the number of key-value mappings in this map.
	 */
	public int size() {
		return count + this.dynProxyMap.size();
	}

	/**
	 * Returns <tt>true</tt> if this map contains no key-value mappings.
	 *
	 * @return <tt>true</tt> if this map contains no key-value mappings.
	 */
	public boolean isEmpty() {
		return count == 0 && this.dynProxyMap.isEmpty();
	}

	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the
	 * specified value.
	 *
	 * @param value value whose presence in this map is to be tested.
	 * @return <tt>true</tt> if this map maps one or more keys to the
	 *         specified value.
	 */
	public synchronized boolean isHidden(Object value, ClassLoader loaderOfKey) {
		Proxy2Hidden tab[] = table;

		if (value == null) {
			for (int i = tab.length; i-- > 0;)
				for (Proxy2Hidden e = tab[i]; e != null; e = e.next)
					if (e.value == null)
						return true;
		} else {
			for (int i = tab.length; i-- > 0;) {
				for (Proxy2Hidden e = tab[i]; e != null; e = e.next) {
					if (value.equals(e.value)) {
						ClassLoader loaderOfProxy = e.key.getClass().getClassLoader();
						if (loaderOfKey.equals(loaderOfProxy)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Returns <tt>true</tt> if this map contains a mapping for the specified
	 * key.
	 * 
	 * @return <tt>true</tt> if this map contains a mapping for the specified
	 * key.
	 * @param key key whose presence in this Map is to be tested.
	 */
	public boolean containsKey(Object key) {
		try {
			return containsProxy((IProxy)key);
		} catch (ClassCastException c) {
			return containsProxy((Proxy)key);
		}
	}

	public synchronized boolean containsProxy(IProxy proxy) {
		Proxy2Hidden tab[] = table;
		if (proxy != null) {
			int hash = proxy.hash();
			int index = (hash & 0x7FFFFFFF) % tab.length;
			for (Proxy2Hidden e = tab[index]; e != null; e = e.next)
				if (e.hash == hash && proxy.same((IProxy)e.key))
					return true;
		} else {
			for (Proxy2Hidden e = tab[0]; e != null; e = e.next)
				if (e.key == null)
					return true;
		}

		return false;
	}
	
	public synchronized boolean containsProxy(Proxy dynProxy) {
			return dynProxyMap.containsKey(dynProxy);
		}

	/**
	 * Rehashes the contents of this map into a new <tt>HashMap</tt> instance
	 * with a larger capacity. This method is called automatically when the
	 * number of keys in this map exceeds its capacity and load factor.
	 */
	private void rehash() {
		synchronized(this) {
		int oldCapacity = table.length;
		Proxy2Hidden oldMap[] = table;

		int newCapacity = oldCapacity * 2 + 1;
		Proxy2Hidden newMap[] = new Proxy2Hidden[newCapacity];

		modCount++;
		threshold = (int) (newCapacity * loadFactor);
		table = newMap;

		for (int i = oldCapacity; i-- > 0;) {
			for (Proxy2Hidden old = oldMap[i]; old != null;) {
				Proxy2Hidden e = old;
				old = old.next;

				int index = (e.hash & 0x7FFFFFFF) % newCapacity;
				e.next = newMap[index];
				newMap[index] = e;
			}
		}
		}
	}
	


	/** get()
	 * Returns the value to which this map maps the specified key.  Returns
	 * <tt>null</tt> if the map contains no mapping for this key.  A return
	 * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
	 * map contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
	 * operation may be used to distinguish these two cases.
	 *
	 * @return the value to which this map maps the specified key.
	 * @param key key whose associated value is to be returned.
	 */

	/** put()
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for this key, the old
	 * value is replaced.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 * @return previous value associated with specified key, or <tt>null</tt>
	 *	       if there was no mapping for key.  A <tt>null</tt> return can
	 *	       also indicate that the HashMap previously associated
	 *	       <tt>null</tt> with the specified key.
	 */
	public synchronized Object register(IProxy proxy, Object original) {
		// Makes sure the key is not already in the HashMap.
		Proxy2Hidden tab[] = table;
		int hash = 0;
		int index = 0;

		if (proxy != null) {
			hash = proxy.hash();
			index = (hash & 0x7FFFFFFF) % tab.length;
			for (Proxy2Hidden e = tab[index]; e != null; e = e.next) {
				if ((e.hash == hash) && proxy.same((IProxy)e.key)) {
					Object old = e.value;
					e.value = original;
					if (old != null) {
						System.err.println("proxies.put ERROR: set value of proxy "+proxy.dump()+" completed, replaced old value "+old+" by new value "+original); 
					}
					return old;
				}
			}
		} else {
			for (Proxy2Hidden e = tab[0]; e != null; e = e.next) {
				if (e.key == null) {
					Object old = e.value;
					e.value = original;
					return old;
				}
			}
		}

		modCount++;
		if (count >= threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			tab = table;
			index = (hash & 0x7FFFFFFF) % tab.length;
		}

		// Creates the new entry. At this execution point, one
		// can be sure the given proxy object is a new key
		Proxy2Hidden e = new Proxy2Hidden(hash, proxy, original, tab[index]);
		tab[index] = e;
		/*if (original.getClass().getName().endsWith("StringEvent")) {
								this.proxy = proxy;
								System.out.println("proxies.put: inserted original "+original+" @ "+index);
								Thread testGetter = new Thread() {
							
									private IProxy key;
									public void run() {
										System.out.println("proxies.get(p4.StringEvent) = "+ProxyRegistry.this.get(getProxy()));
										
									}
								};
								testGetter.start();
							}*/
		count++;
		return null;
	}

	/**
	 * Removes the mapping for this key from this map if present.
	 *
	 * @param key key whose mapping is to be removed from the map.
	 * @return previous value associated with specified key, or <tt>null</tt>
	 *	       if there was no mapping for key.  A <tt>null</tt> return can
	 *	       also indicate that the map previously associated <tt>null</tt>
	 *	       with the specified key.
	 */
	public Object remove(Object key) {
		try {
			return removeProxy((IProxy) key);
		} catch (ClassCastException c) {
			synchronized(this.dynProxyMap) {
				return (Proxy) this.dynProxyMap.remove(key);
			}
		}
	}

	public synchronized Object removeProxy(IProxy proxy) {
		Proxy2Hidden tab[] = table;

		if (proxy != null) {
			int hash = proxy.hash();
			int index = (hash & 0x7FFFFFFF) % tab.length;

			for (Proxy2Hidden e = tab[index], prev = null; e != null; prev = e, e = e.next) {
				if ((e.hash == hash) && proxy.same((IProxy)e.key)) {
					modCount++;
					if (prev != null)
						prev.next = e.next;
					else
						tab[index] = e.next;

					count--;
					Object oldValue = e.value;
					this.hidden2unstableproxy.remove(oldValue);
					this.hidden2stableproxy.remove(oldValue);
					e.value = null;
					return oldValue;
				}
			}
		} else {
			for (Proxy2Hidden e = tab[0], prev = null; e != null; prev = e, e = e.next) {
				if (e.key == null) {
					modCount++;
					if (prev != null)
						prev.next = e.next;
					else
						tab[0] = e.next;

					count--;
					Object oldValue = e.value;
					e.value = null;
					return oldValue;
				}
			}
		}

		return null;
	}

	/**
	 * Copies all of the mappings from the specified map to this one.
	 * 
	 * These mappings replace any mappings that this map had for any of the
	 * keys currently in the specified Map.
	 *
	 * @param t Mappings to be stored in this map.
	 */
	public void putAll(Map t) {
		Iterator i = t.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			put(e.getKey(), e.getValue());
		}
	}

	/**
	 * Removes all mappings from this map.
	 */
	public void clear() {
		synchronized(this) {
		Proxy2Hidden tab[] = table;
		modCount++;
		for (int index = tab.length; --index >= 0;)
			tab[index] = null;
		count = 0;
		}
		synchronized(this.dynProxyMap) {
			this.dynProxyMap.clear();
		}
	}

	/**
	 * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
	 * values themselves are not cloned.
	 *
	 * @return a shallow copy of this map.
	 */
	public Object clone() {
		try {
			ProxyRegistry t = (ProxyRegistry) super.clone();
			t.table = new Proxy2Hidden[table.length];
			for (int i = table.length; i-- > 0;) {
				t.table[i] = (table[i] != null) ? (Proxy2Hidden) table[i].clone() : null;
			}
			t.keySet = null;
			t.entrySet = null;
			t.values = null;
			t.modCount = 0;
			return t;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}

	// Views

	private transient Set keySet = null;
	private transient Set entrySet = null;
	private transient Collection values = null;

	/**
	 * Returns a set view of the keys contained in this map.  The set is
	 * backed by the map, so changes to the map are reflected in the set, and
	 * vice-versa.  The set supports element removal, which removes the
	 * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
	 * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
	 * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
	 * <tt>addAll</tt> operations.
	 *
	 * @return a set view of the keys contained in this map.
	 */
	public Set keySet() {
		if (keySet == null) {
			keySet = new AbstractSet() {
				public Iterator iterator() {
					return ProxyRegistry.this.getHashIterator(KEYS);
				}
				public int size() {
					return ProxyRegistry.this.count;
				}
				public boolean contains(Object o) {
					return ProxyRegistry.this.containsKey(o);
				}
				public boolean remove(Object o) {
					int oldSize = ProxyRegistry.this.count;
					ProxyRegistry.this.remove(o);
					return count != oldSize;
				}
				public void clear() {
					ProxyRegistry.this.clear();
				}
			};
		}
		return keySet;
	}

	/**
	 * Returns a collection view of the values contained in this map.  The
	 * collection is backed by the map, so changes to the map are reflected in
	 * the collection, and vice-versa.  The collection supports element
	 * removal, which removes the corresponding mapping from this map, via the
	 * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
	 * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
	 * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @return a collection view of the values contained in this map.
	 */
	public Collection values() {
		if (values == null) {
			values = new AbstractCollection() {
				public Iterator iterator() {
					return ProxyRegistry.this.getHashIterator(VALUES);
				}
				public int size() {
					return ProxyRegistry.this.count;
				}
				public boolean contains(Object o) {
					return ProxyRegistry.this.containsValue(o);
				}
				public void clear() {
					ProxyRegistry.this.clear();
				}
			};
		}
		return values;
	}

	/**
	 * Returns a collection view of the mappings contained in this map.  Each
	 * element in the returned collection is a <tt>Map.Proxy2Hidden</tt>.  The
	 * collection is backed by the map, so changes to the map are reflected in
	 * the collection, and vice-versa.  The collection supports element
	 * removal, which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
	 * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
	 * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @return a collection view of the mappings contained in this map.
	 * @see Map.Proxy2Hidden
	 */
	public Set entrySet() {
		if (entrySet == null) {
			entrySet = new AbstractSet() {
				public Iterator iterator() {
					return ProxyRegistry.this.getHashIterator(ENTRIES);
				}

				public boolean contains(Object o) {
					if (!(o instanceof Map.Entry))
						return false;
					Map.Entry entry = (Map.Entry) o;
					Object key = entry.getKey();
					Proxy2Hidden tab[] = ProxyRegistry.this.table;
					int hash = (key == null ? 0 : ((IProxy)key).hash());
					int index = (hash & 0x7FFFFFFF) % tab.length;

					for (Proxy2Hidden e = tab[index]; e != null; e = e.next)
						if (e.hash == hash && e.equals(entry))
							return true;
					return false;
				}

				public boolean remove(Object o) {
					if (!(o instanceof Map.Entry))
						return false;
					Map.Entry entry = (Map.Entry) o;
					Object key = entry.getKey();
					Proxy2Hidden tab[] = ProxyRegistry.this.table;
					int hash = (key == null ? 0 : ((IProxy)key).hash());
					int index = (hash & 0x7FFFFFFF) % tab.length;

					for (Proxy2Hidden e = tab[index], prev = null; e != null; prev = e, e = e.next) {
						if (e.hash == hash && e.equals(entry)) {
							modCount++;
							if (prev != null)
								prev.next = e.next;
							else
								tab[index] = e.next;

							count--;
							e.value = null;
							return true;
						}
					}
					return false;
				}

				public int size() {
					return ProxyRegistry.this.count;
				}

				public void clear() {
					ProxyRegistry.this.clear();
				}
			};
		}

		return entrySet;
	}

	private Iterator getHashIterator(int type) {
		if (count == 0) {
			return emptyHashIterator;
		} else {
			return new HashIterator(type);
		}
	}

	/**
	 * HashMap collision list entry.
	 */
	private static class Proxy2Hidden implements Map.Entry {
		int hash;
		IProxy key;
		Object value;
		Proxy2Hidden next;

		Proxy2Hidden(int hash, IProxy key, Object value, Proxy2Hidden next) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = next;
			//System.out.println(" new Entry("+hash+", "+key+", "+value+", "+next+")");
		}

		protected Object clone() {
			return new Proxy2Hidden(hash, key, value, (next == null ? null : (Proxy2Hidden) next.clone()));
		}

		// Map.Entry Ops 

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public Object setValue(Object value) {
			Object oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry e = (Map.Entry) o;

			return (key == null ? e.getKey() == null : ((IProxy)key).equals(e.getKey()))
				&& (value == null ? e.getValue() == null : value.equals(e.getValue()));
		}

		public int hashCode() {
			return hash ^ (value == null ? 0 : value.hashCode());
		}

		public String toString() {
			return key.dump() + "=" + value;
		}
	}

	// Types of Iterators
	private static final int KEYS = 0;
	private static final int VALUES = 1;
	private static final int ENTRIES = 2;

	private static EmptyHashIterator emptyHashIterator = new EmptyHashIterator();

	private static class EmptyHashIterator implements Iterator {

		EmptyHashIterator() {

		}

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new IllegalStateException();
		}

	}

	private class HashIterator implements Iterator {
		Proxy2Hidden[] table = ProxyRegistry.this.table;
		int index = table.length;
		Proxy2Hidden entry = null;
		Proxy2Hidden lastReturned = null;
		int type;

		/**
		 * The modCount value that the iterator believes that the backing
		 * List should have.  If this expectation is violated, the iterator
		 * has detected concurrent modification.
		 */
		private int expectedModCount = modCount;

		HashIterator(int type) {
			this.type = type;
		}

		public boolean hasNext() {
			Proxy2Hidden e = entry;
			int i = index;
			Proxy2Hidden t[] = table;
			/* Use locals for faster loop iteration */
			while (e == null && i > 0)
				e = t[--i];
			entry = e;
			index = i;
			return e != null;
		}

		public Object next() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();

			Proxy2Hidden et = entry;
			int i = index;
			Proxy2Hidden t[] = table;

			/* Use locals for faster loop iteration */
			while (et == null && i > 0)
				et = t[--i];

			entry = et;
			index = i;
			if (et != null) {
				Proxy2Hidden e = lastReturned = entry;
				entry = e.next;
				return type == KEYS ? e.key : (type == VALUES ? e.value : e);
			}
			throw new NoSuchElementException();
		}

		public void remove() {
			if (lastReturned == null)
				throw new IllegalStateException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();

			Proxy2Hidden[] tab = ProxyRegistry.this.table;
			int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;

			for (Proxy2Hidden e = tab[index], prev = null; e != null; prev = e, e = e.next) {
				if (e == lastReturned) {
					modCount++;
					expectedModCount++;
					if (prev == null)
						tab[index] = e.next;
					else
						prev.next = e.next;
					count--;
					lastReturned = null;
					return;
				}
			}
			throw new ConcurrentModificationException();
		}
	}

	/**
	 * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
	 * serialize it).
	 *
	 * @serialData The <i>capacity</i> of the HashMap (the length of the
	 *		   bucket array) is emitted (int), followed  by the
	 *		   <i>size</i> of the HashMap (the number of key-value
	 *		   mappings), followed by the key (Object) and value (Object)
	 *		   for each key-value mapping represented by the HashMap
	 * The key-value mappings are emitted in no particular order.
	 */
	private void writeObject(java.io.ObjectOutputStream s) throws IOException {
		// Write out the threshold, loadfactor, and any hidden stuff
		s.defaultWriteObject();

		// Write out number of buckets
		s.writeInt(table.length);

		// Write out size (number of Mappings)
		s.writeInt(count);

		// Write out keys and values (alternating)
		for (int index = table.length - 1; index >= 0; index--) {
			Proxy2Hidden entry = table[index];

			while (entry != null) {
				s.writeObject(entry.key);
				s.writeObject(entry.value);
				entry = entry.next;
			}
		}
	}

	private static final long serialVersionUID = 362498820763181265L;

	private static Logger LOG = Logger.getLogger(ProxyRegistry.class);

	/**
	 * Reconstitute the <tt>HashMap</tt> instance from a stream (i.e.,
	 * deserialize it).
	 */
	private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
		// Read in the threshold, loadfactor, and any hidden stuff
		s.defaultReadObject();

		// Read in number of buckets and allocate the bucket array;
		int numBuckets = s.readInt();
		table = new Proxy2Hidden[numBuckets];

		// Read in size (number of Mappings)
		int size = s.readInt();

		// Read the keys and values, and put the mappings in the HashMap
		for (int i = 0; i < size; i++) {
			Object key = s.readObject();
			Object value = s.readObject();
			put(key, value);
		}
	}

	int capacity() {
		return table.length;
	}

	float loadFactor() {
		return loadFactor;
	}

	/**
	 * Returns the key of a given value
	 * 
	 * @param originalReturnedObject
	 * @return - the stable IProxy hiding it
	 */
	public synchronized IProxy getStableProxyOf(Object value) {
		Object key = this.hidden2stableproxy.get(value);
		if (key instanceof IProxy) {
			return (IProxy)key;
		}
		Proxy2Hidden tab[] = table;
		if (value == null) {
			for (int i = tab.length; i-- > 0;)
				for (Proxy2Hidden e = tab[i]; e != null; e = e.next)
					if (e.value == null)
						return e.key;
		} else {
			for (int i = tab.length; i-- > 0;) {
				for (Proxy2Hidden e = tab[i]; e != null; e = e.next) {
					if (value.equals(e.value)) {
						ClassLoader loader = e.key.getClass().getClassLoader();
						if (loader instanceof ProxyLoader) {
							this.hidden2stableproxy.put(value,e.key);
							return e.key;
						}
					}
				}
			}
		}
		return null;
	}
	
	private Hashtable hidden2stableproxy = new Hashtable(30);
	private Hashtable hidden2unstableproxy = new Hashtable(30);
	
	
	/**
	 * Returns the key of a given value
	 * 
	 * @param originalReturnedObject
	 * @return - the stable IProxy hiding it
	 */
	public synchronized IProxy getUnstableProxyOf(Object value) {
		Object key = this.hidden2unstableproxy.get(value);
		if (key instanceof IProxy) {
			return (IProxy)key;
		}
		Proxy2Hidden tab[] = this.table;
		if (value == null) {
			for (int i = tab.length; i-- > 0;)
				for (Proxy2Hidden e = tab[i]; e != null; e = e.next)
					if (e.value == null)
						return e.key;
		} else {
			for (int i = tab.length; i-- > 0;) {
				for (Proxy2Hidden e = tab[i]; e != null; e = e.next) {
					if (value.equals(e.value)) {
						ClassLoader loader = e.key.getClass().getClassLoader();
						if (loader instanceof AdapterClassLoader) {
							this.hidden2unstableproxy.put(value,e.key);
							return e.key;
						}
					}
				}
			}
		}
		return null;
	}
	
	
	public synchronized Proxy getProxyOf(Object hidden) {
		
		// dynProxyMap.containsValue(value);
		Iterator dynProxies = dynProxyMap.entrySet().iterator();
		while (dynProxies.hasNext()) {
			Map.Entry element = (Map.Entry) dynProxies.next();
			if (element.getValue().equals(hidden)) {
				return (Proxy) element.getKey();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		try {
			IProxy p = getStableProxyOf(value);
			if (p == null) {
				return getUnstableProxyOf(value) != null;
			}
		} catch (ClassCastException c) {
			return getProxyOf(value) != null;
		}
		return false;
	}

	/**
	 * 
	 * for dynamic proxy only !!!!
	 * @param oldObj
	 * @param newObj
	 */
	public synchronized void updateHiddenObject(Object oldObj, Object newObj) {
		Proxy proxy = null; // transparent proxy
			// TODO: How to match NON singleton objects (how to find corresponding objects in both version, how to access these)
			// original may occur more than once
			Iterator mapping = dynProxyMap.entrySet().iterator();
			while (mapping.hasNext()) {
				Map.Entry element = (Map.Entry) mapping.next();
				if (element.getValue() == oldObj) {
					element.setValue(newObj);
					proxy = (Proxy) element.getKey();
					LOG.info(" proxy for " + oldObj + " now redirects to " + newObj);
					IAdapter redirectToOriginal = DefaultAdapter.Instance();
					//proxy.setInvocationHandler(redirectToOriginal); is default handler
					redirectToOriginal.setOriginal(newObj);
					
					//proxy.notify(); // **************************************************** synchronization NOTIFY
				}
			}
			if (proxy == null) {
						LOG.info(" (cop was not in use?) unknown dynamic proxy for " + oldObj);
						return;
			}
	}

	/**
	 * @return
	 */
	public Iterator dynamicProxies() {
		LOG.info("getting an iterator for dynamic proxies");
		return this.dynProxyMap.keySet().iterator();
	}

} 
