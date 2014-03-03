package org.freyja.jdbc.sharding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freyja.jdbc.mapping.ShardingEntity;
import org.freyja.jdbc.mapping.ShardingProperty;
import org.freyja.jdbc.object.DbResult;
import org.freyja.jdbc.parser.ShardingUtil;
import org.freyja.jdbc.utils.ListUtil;

import com.alibaba.druid.mapping.Property;

public class FreyjaEntity<T> extends ShardingEntity<T> {

	private Map<String, String> sqlMap = new HashMap<String, String>();

	private Map<String, int[]> sqlTyps = new HashMap<String, int[]>();

	public int[] getSqlTyps(String key) {
		return sqlTyps.get(key);
	}

	public String toSelect() {
		String sql = sqlMap.get("select");
		if (sql != null) {
			return sql;
		}
		sql = "select * from " + getTableName() + " where "
				+ getId().getDbColumnName() + " = ?";
		sqlMap.put("select", sql);

		sqlTyps.put("select", new int[] { getId().getTypes() });

		return sql;
	}

	public DbResult toSelect(Object idValue) {
		DbResult result = ShardingUtil.engine.getShardingStrategy()
				.getShardingTableNameById(getTableName(), idValue);
		String sql = "select * from " + result.getTableName() + " where "
				+ getId().getDbColumnName() + " = ?";
		result.setSql(sql);
		return result;
	}

	public String toUpdate() {

		String sql = sqlMap.get("update");
		if (sql != null) {
			return sql;
		}

		String set = "";
		List<Integer> types = new ArrayList<Integer>();
		for (Property p : getProperties().values()) {
			ShardingProperty s = (ShardingProperty) p;
			if (s.isId()) {
				continue;
			}
			set += s.getDbColumnName() + " = ? ,";
			types.add(s.getTypes());
		}

		set = set.substring(0, set.length() - 1);

		String update = " set " + set + " where " + getId().getDbColumnName()
				+ " = ?";
		sqlMap.put("update_va", update);

		types.add(getId().getTypes());
		sqlTyps.put("update", ListUtil.toPrimitive(types));

		sql = "update " + getTableName() + update;
		sqlMap.put("update", sql);
		return sql;
	}

	public DbResult toUpdate(Object idValue) {
		DbResult result = ShardingUtil.engine.getShardingStrategy()
				.getShardingTableNameById(getTableName(), idValue);
		String sql = "update " + result.getTableName()
				+ sqlMap.get("update_va");
		result.setSql(sql);
		return result;
	}

	public String toDelete() {

		String sql = sqlMap.get("delete");
		if (sql != null) {
			return sql;
		}

		sql = "delete from " + getTableName() + " where "
				+ getId().getDbColumnName() + " = ?";
		sqlMap.put("delete", sql);
		sqlTyps.put("delete", new int[] { getId().getTypes() });
		return sql;
	}

	public DbResult toDelete(Object idValue) {
		DbResult result = ShardingUtil.engine.getShardingStrategy()
				.getShardingTableNameById(getTableName(), idValue);
		String sql = "delete from " + result.getTableName() + " where "
				+ getId().getDbColumnName() + " = ?";

		result.setSql(sql);
		return result;
	}

	public String toInsert() {
		String sql = sqlMap.get("insert");
		if (sql != null) {
			return sql;
		}
		String columns = "";
		String values = "";
		List<Integer> types = new ArrayList<Integer>();
		for (Property p : getProperties().values()) {
			ShardingProperty s = (ShardingProperty) p;

			if (s.isId() && s.isAuto()) {
				continue;
			}
			columns += s.getDbColumnName() + " ,";
			values += "? ,";

			types.add(s.getTypes());
		}

		columns = columns.substring(0, columns.length() - 1);
		values = values.substring(0, values.length() - 1);

		String va = " (" + columns + ") values (" + values + ")";

		sqlMap.put("insert_va", va);

		sqlTyps.put("insert", ListUtil.toPrimitive(types));

		sql = "insert into " + getTableName() + va;
		sqlMap.put("insert", sql);
		return sql;
	}

	public DbResult toInsert(Object subValue) {
		DbResult result = ShardingUtil.engine.getShardingStrategy()
				.getShardingTableName(getTableName(), subValue);
		String sql = "insert into " + result.getTableName()
				+ sqlMap.get("insert_va");
		result.setSql(sql);
		return result;
	}

	public void init() {

		toInsert();

		// if (getId() == null) {
		// return;
		// }

		toDelete();
		toSelect();

		toUpdate();
	}

}
