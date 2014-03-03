package org.freyja.jdbc.object;

import com.alibaba.druid.sql.ast.SQLStatementImpl;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;

public class ExplainResult {

	private String jdbcSql;

	private String tableName;

	private int dbNo = -1;

	private boolean needSharding;

	private SQLStatementImpl stmt;

	private SQLSelectQueryBlock query;

	private SQLInsertStatement insert;

	public SQLInsertStatement getInsert() {
		return insert;
	}

	public void setInsert(SQLInsertStatement insert) {
		this.insert = insert;
	}

	public ExplainResult(String jdbcSql, String tableName,
			boolean needSharding, SQLStatementImpl stmt) {
		this.jdbcSql = jdbcSql;
		this.tableName = tableName;
		this.needSharding = needSharding;
		this.stmt = stmt;
	}

	public ExplainResult(String jdbcSql, String tableName,
			boolean needSharding, SQLSelectQueryBlock query) {
		this.jdbcSql = jdbcSql;
		this.tableName = tableName;
		this.needSharding = needSharding;
		this.query = query;
	}

	public ExplainResult(String jdbcSql, String tableName,
			boolean needSharding, SQLInsertStatement insert) {
		this.jdbcSql = jdbcSql;
		this.tableName = tableName;
		this.needSharding = needSharding;
		this.insert = insert;
	}

	public SQLSelectQueryBlock getQuery() {
		return query;
	}

	public void setQuery(SQLSelectQueryBlock query) {
		this.query = query;
	}

	public String getJdbcSql() {
		return jdbcSql;
	}

	public void setJdbcSql(String jdbcSql) {
		this.jdbcSql = jdbcSql;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean isNeedSharding() {
		return needSharding;
	}

	public void setNeedSharding(boolean needSharding) {
		this.needSharding = needSharding;
	}

	public SQLStatementImpl getStmt() {
		return stmt;
	}

	public void setStmt(SQLStatementImpl stmt) {
		this.stmt = stmt;
	}

	public int getDbNo() {
		return dbNo;
	}

	public void setDbNo(int dbNo) {
		this.dbNo = dbNo;
	}

}
