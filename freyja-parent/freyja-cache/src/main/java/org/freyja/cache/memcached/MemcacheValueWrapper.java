package org.freyja.cache.memcached;

import java.io.Serializable;

import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.SimpleValueWrapper;

public class MemcacheValueWrapper implements ValueWrapper, Serializable {

	private Object value;

	public MemcacheValueWrapper() {

	}

	/**
	 * Create a new SimpleValueWrapper instance for exposing the given value.
	 * 
	 * @param value
	 *            the value to expose (may be {@code null})
	 */
	public MemcacheValueWrapper(Object value) {
		this.value = value;
	}

	/**
	 * Simply returns the value as given at construction time.
	 */
	public Object get() {
		return this.value;
	}

}
