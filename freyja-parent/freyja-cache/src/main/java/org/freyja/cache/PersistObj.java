package org.freyja.cache;

import java.io.Serializable;

public class PersistObj implements Serializable {

	protected boolean serialize = true;

	public boolean isSerialize() {
		return serialize;
	}

	public void setSerialize(boolean serialize) {
		this.serialize = serialize;
	}

}
