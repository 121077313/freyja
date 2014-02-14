package org.freyja.jdbc.core.rowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.freyja.jdbc.mapping.ShardingEntity;
import org.freyja.jdbc.parser.ShardingUtil;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import com.alibaba.druid.mapping.Property;

public class MapRowMapper extends ColumnMapRowMapper {

	@Override
	public Map<String, Object> mapRow(ResultSet rs, int rowNum)
			throws SQLException {

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, Object> mapOfColValues = createColumnMap(columnCount);
		for (int i = 1; i <= columnCount; ++i) {
			String key = getColumnKey(rsmd.getTableName(i),
					JdbcUtils.lookupColumnName(rsmd, i));
			Object obj = getColumnValue(rs, i);
			mapOfColValues.put(key, obj);
		}
		return mapOfColValues;

	}

	protected String getColumnKey(String tableName, String columnName) {

		if (tableName == null) {
			return columnName;
		}
		ShardingEntity<?> entity = ShardingUtil.getEntity(tableName);
		if (entity == null) {
			return columnName;
		}

		Property property = entity.getProperty(columnName);
		if (property == null) {
			return columnName;
		}

		return property.getName();
	}
}