package org.freyja.cache.memcached;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleCacheManager;

import com.whalin.MemCached.MemCachedClient;

public class MemcachedCacheManager extends SimpleCacheManager {
	static Logger log = LoggerFactory.getLogger(MemcachedCacheManager.class);
	private Collection<String> names;

	private MemCachedClient store;

	private long expiry = 1000l * 60 * 60 * 24 * 2;

	public Collection<String> getNames() {
		return names;
	}

	public void setNames(Collection<String> names) {
		this.names = names;
	}

	public MemCachedClient getStore() {
		return store;
	}

	public void setStore(MemCachedClient store) {
		this.store = store;
	}

	@Override
	protected Collection<? extends Cache> loadCaches() {
		return new ArrayList<Cache>();
	}

	@Override
	public Cache getCache(String name) {
		Cache cache = super.getCache(name);
		if (cache == null) {
			synchronized (store) {
				cache = super.getCache(name);
				if (cache == null) {
					cache = new MemcachedCache(store, name, expiry);
					addCache(cache);

					log.debug("动态创建缓存空间:{}", name);
				}
			}
		}

		return cache;
	}

	public long getExpiry() {
		return expiry;
	}

	public void setExpiry(long expiry) {
		this.expiry = expiry;
	}

}
