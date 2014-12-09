package org.freyja.cache.memcached;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.cache.Cache;
import org.springframework.util.StringUtils;

import com.whalin.MemCached.MemCachedClient;

public class MemcachedCache implements Cache {

	/** 3天后过期 */
	long expiry;

	private final String name;

	final int retryNum = 2;

	private final MemCachedClient store;

	public MemcachedCache(MemCachedClient store, String name, long expiry) {
		this.store = store;
		this.name = name;
		this.expiry = expiry;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return store;
	}

	@Override
	public ValueWrapper get(Object key) {
		ValueWrapper element = (ValueWrapper) store.get(getKey(key));

		return element;
	}

	@Override
	public void put(Object key, Object value) {
		Date expiryDate = new Date(expiry);
		String keyString = getKey(key);

		boolean stored = store.set(keyString, new MemcacheValueWrapper(value),
				expiryDate);
		if (!stored) {
			for (int i = 0; i < retryNum; i++) {
				// 重试
				stored = store.set(keyString, new MemcacheValueWrapper(value),
						expiryDate);
				if (stored) {// 重试成功
					return;
				}
			}

			if (value instanceof Collection) {
				throw new RuntimeException("缓存存储失败!key:"
						+ keyString
						+ ",value:"
						+ StringUtils.collectionToDelimitedString(
								(Collection<?>) value, ","));

			}
			throw new RuntimeException("缓存存储失败!key:" + getKey(key) + ",value:"
					+ value);
		}

	}

	public static void main(String[] args) {
		List<String> key = new ArrayList<String>();
		key.add("a");
		key.add("b");
		System.out.println(StringUtils.collectionToDelimitedString(
				(Collection<?>) key, ","));
	}

	@Override
	public void evict(Object key) {
		store.delete(getKey(key));
	}

	String getKey(Object key) {
		return new StringBuffer(name).append("_").append(key).toString();
	}

	@Override
	public void clear() {
		store.flushAll();
	}

}
