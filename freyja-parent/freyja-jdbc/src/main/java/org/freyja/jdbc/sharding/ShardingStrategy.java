package org.freyja.jdbc.sharding;

import org.freyja.jdbc.object.DbResult;

public interface ShardingStrategy {

	public DbResult getShardingTableName(String tableName, Object value);

	public DbResult getShardingTableNameById(String tableName, Object idValue);

	public int getTableNum();

	public int getIdSubNum();

	public int getDbNum();

}
