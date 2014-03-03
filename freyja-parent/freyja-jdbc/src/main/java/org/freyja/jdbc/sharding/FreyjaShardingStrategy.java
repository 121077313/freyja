package org.freyja.jdbc.sharding;

import org.freyja.jdbc.object.DbResult;

public class FreyjaShardingStrategy implements ShardingStrategy {

	private int tableNum;
	private int dbNum;
	private int idSubNum;

	@Override
	public DbResult getShardingTableName(String tableName, Object value) {
		int hashCode = value.hashCode();
		if (hashCode < 0) {
			hashCode = -hashCode;
		}
		int dbNo = hashCode % getDbNum();
		int tableNo = hashCode % getTableNum();
		tableName = tableName + "_" + tableNo;
		return new DbResult(tableName, tableNo, dbNo);
	}

	@Override
	public DbResult getShardingTableNameById(String tableName, Object idValue) {
		if (idValue == null) {
			return new DbResult(tableName, -1, -1);
		} else {
			int n = (Integer) idValue / getIdSubNum();
			int dbNo = n / getTableNum();
			int tableNo = n % getTableNum();
			tableName = tableName + "_" + tableNo;
			return new DbResult(tableName, tableNo, dbNo);
		}
	}

	public int getTableNum() {
		return tableNum;
	}

	public void setTableNum(int tableNum) {
		this.tableNum = tableNum;
	}

	public int getDbNum() {
		return dbNum;
	}

	public void setDbNum(int dbNum) {
		this.dbNum = dbNum;
	}

	public int getIdSubNum() {
		return idSubNum;
	}

	public void setIdSubNum(int idSubNum) {
		this.idSubNum = idSubNum;
	}

}
