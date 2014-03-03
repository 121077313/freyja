package org.freyja.cache.operation;

import org.springframework.cache.interceptor.CacheOperation;

public class FreyjaCacheOperation extends CacheOperation {
	private boolean keyValueFromReturn;

	public boolean isKeyValueFromReturn() {
		return keyValueFromReturn;
	}

	public void setKeyValueFromReturn(boolean keyValueFromReturn) {
		this.keyValueFromReturn = keyValueFromReturn;
	}

}
