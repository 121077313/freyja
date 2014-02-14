package org.freyja.jdbc.ds;

public class DbContextHolder {
	private static final ThreadLocal<Integer> contextHolder = new ThreadLocal<Integer>();

	public static void setDbNum(Integer dbNum) {
		contextHolder.set(dbNum);
	}

	public static Integer getDbNum() {
		return contextHolder.get();
	}

}
