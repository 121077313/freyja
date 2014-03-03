package org.freyja.jdbc.object;

public class DbResult {
	private String tableName;
	private int tableNo = -1;
	private int dbNo = -1;
	private String sql;

	private boolean needSharding;

	private boolean needCycle;

	public DbResult() {
	}

	public DbResult(String tableName, int tableNo, int dbNo) {

		this.tableName = tableName;
		this.tableNo = tableNo;
		this.dbNo = dbNo;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getTableNo() {
		return tableNo;
	}

	public void setTableNo(int tableNo) {
		this.tableNo = tableNo;
	}

	public int getDbNo() {
		return dbNo;
	}

	public void setDbNo(int dbNo) {
		this.dbNo = dbNo;
	}

	public boolean isNeedSharding() {
		return needSharding;
	}

	public void setNeedSharding(boolean needSharding) {
		this.needSharding = needSharding;
	}

	public boolean isNeedCycle() {
		return needCycle;
	}

	public void setNeedCycle(boolean needCycle) {
		this.needCycle = needCycle;
	}

}
