package org.freyja.jdbc.mapping;

import java.util.HashMap;
import java.util.Map;

import org.freyja.jdbc.sharding.ShardingStrategy;

import com.alibaba.druid.mapping.Entity;
import com.alibaba.druid.mapping.Property;

public class ShardingEntity<T> extends Entity {

	private boolean isSubTable;

	private boolean saveAsync;

	private boolean updateAsync;

	private Class<T> clazz;

	@Override
	public void addProperty(Property property) {

		ShardingProperty p = (ShardingProperty) property;
		if (p.isId()) {
			propertyCache.put("id", p);
		} else if (p.isSubColumn()) {
			propertyCache.put("subColumn", p);
		}

		super.addProperty(p);
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public boolean isSubTable() {
		return isSubTable;
	}

	public void setSubTable(boolean isSubTable) {
		this.isSubTable = isSubTable;
	}

	private Map<String, ShardingProperty> propertyCache = new HashMap<String, ShardingProperty>();

	public ShardingProperty getId() {
		return propertyCache.get("id");
	}

	public ShardingProperty getSubColumn() {
		return propertyCache.get("subColumn");
	}

	public boolean isSaveAsync() {
		return saveAsync;
	}

	public void setSaveAsync(boolean saveAsync) {
		this.saveAsync = saveAsync;
	}

	public boolean isUpdateAsync() {
		return updateAsync;
	}

	public void setUpdateAsync(boolean updateAsync) {
		this.updateAsync = updateAsync;
	}

}