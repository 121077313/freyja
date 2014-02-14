package org.freyja.jdbc.object;

public class Parameter {

	private String sql;

	private int dbNo = -1;

	private Object[] args;

	private int[] sqlTypes;

	public Parameter(Object[] args, int[] sqlTypes) {

		this.args = args;
		this.sqlTypes = sqlTypes;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public int[] getSqlTypes() {
		return sqlTypes;
	}

	public void setSqlTypes(int[] sqlTypes) {
		this.sqlTypes = sqlTypes;
	}

	public int getDbNo() {
		return dbNo;
	}

	public void setDbNo(int dbNo) {
		this.dbNo = dbNo;
	}

}
