package org.freyja.jdbc.mapping;

import org.freyja.jdbc.utils.StatementCreatorUtils;

import com.alibaba.druid.mapping.Property;

public class ShardingProperty extends Property {

	private boolean isId;

	private boolean isSubColumn;

	private boolean auto;

	private int types;

	public ShardingProperty(Class<?> clazz, String name, String dbColumnName) {
		super(name, null, dbColumnName);
		this.types = StatementCreatorUtils.javaTypeToSqlParameterType(clazz);
	}

	public boolean isId() {
		return isId;
	}

	public void setId(boolean isId) {
		this.isId = isId;
	}

	public boolean isSubColumn() {
		return isSubColumn;
	}

	public void setSubColumn(boolean isSubColumn) {
		this.isSubColumn = isSubColumn;
	}

	public int getTypes() {
		return types;
	}

	public void setTypes(int types) {
		this.types = types;
	}

	public boolean isAuto() {
		return auto;
	}

	public void setAuto(boolean auto) {
		this.auto = auto;
	}

}
