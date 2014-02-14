package org.freyja.jdbc.utils;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Types;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatementCreatorUtils {

	private static Map<Class, Integer> javaTypeToSqlTypeMap = new HashMap<Class, Integer>(
			32);
	public static final int TYPE_UNKNOWN = -2147483648;
	static {
		javaTypeToSqlTypeMap.put(byte.class, Types.TINYINT);
		javaTypeToSqlTypeMap.put(Byte.class, Types.TINYINT);
		javaTypeToSqlTypeMap.put(short.class, Types.SMALLINT);
		javaTypeToSqlTypeMap.put(Short.class, Types.SMALLINT);
		javaTypeToSqlTypeMap.put(int.class, Types.INTEGER);
		javaTypeToSqlTypeMap.put(Integer.class, Types.INTEGER);
		javaTypeToSqlTypeMap.put(long.class, Types.BIGINT);
		javaTypeToSqlTypeMap.put(Long.class, Types.BIGINT);
		javaTypeToSqlTypeMap.put(BigInteger.class, Types.BIGINT);
		javaTypeToSqlTypeMap.put(float.class, Types.FLOAT);
		javaTypeToSqlTypeMap.put(Float.class, Types.FLOAT);
		javaTypeToSqlTypeMap.put(double.class, Types.DOUBLE);
		javaTypeToSqlTypeMap.put(Double.class, Types.DOUBLE);
		javaTypeToSqlTypeMap.put(BigDecimal.class, Types.DECIMAL);
		javaTypeToSqlTypeMap.put(java.sql.Date.class, Types.DATE);
		javaTypeToSqlTypeMap.put(java.sql.Time.class, Types.TIME);
		javaTypeToSqlTypeMap.put(java.sql.Timestamp.class, Types.TIMESTAMP);
		javaTypeToSqlTypeMap.put(Blob.class, Types.BLOB);
		javaTypeToSqlTypeMap.put(Clob.class, Types.CLOB);
	}

	/**
	 * Derive a default SQL type from the given Java type.
	 * 
	 * @param javaType
	 *            the Java type to translate
	 * @return the corresponding SQL type, or <code>null</code> if none found
	 */
	public static int javaTypeToSqlParameterType(Class javaType) {
		Integer sqlType = javaTypeToSqlTypeMap.get(javaType);
		if (sqlType != null) {
			return sqlType;
		}
		if (Number.class.isAssignableFrom(javaType)) {
			return Types.NUMERIC;
		}
		if (isStringValue(javaType)) {
			return Types.VARCHAR;
		}
		if (isDateValue(javaType) || Calendar.class.isAssignableFrom(javaType)) {
			return Types.TIMESTAMP;
		}
		return TYPE_UNKNOWN;
	}

	/**
	 * Check whether the given value can be treated as a String value.
	 */
	private static boolean isStringValue(Class inValueType) {
		// Consider any CharSequence (including StringBuffer and StringBuilder)
		// as a String.
		return (CharSequence.class.isAssignableFrom(inValueType) || StringWriter.class
				.isAssignableFrom(inValueType));
	}

	/**
	 * Check whether the given value is a <code>java.util.Date</code> (but not
	 * one of the JDBC-specific subclasses).
	 */
	private static boolean isDateValue(Class inValueType) {
		return (java.util.Date.class.isAssignableFrom(inValueType) && !(java.sql.Date.class
				.isAssignableFrom(inValueType)
				|| java.sql.Time.class.isAssignableFrom(inValueType) || java.sql.Timestamp.class
					.isAssignableFrom(inValueType)));
	}
}
