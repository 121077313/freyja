package org.freyja.data.dao;

import java.util.List;

import org.freyja.jdbc.core.FreyjaJdbcOperations;

public interface ICommonDao extends FreyjaJdbcOperations {

	/**
	 * 封装query方法，where 为查询条件
	 * 
	 * @param hql
	 *            例:get(User.class,"uid = ? and level = ?",5,1)
	 * @param hql
	 *            或:get(User.class,"uid = 1")
	 */
	<T> T get(Class<T> clazz, String where, Object... args);

	/**
	 * 封装query方法，根据 hql 查询单个泛型对象 ，如果不是查询所有列 * 则返回多个列的结果
	 * 
	 * @param hql
	 *            例:get("select * from User where id = ?",1)
	 * @param hql
	 *            或:get("select name,level from User where id = ?",1)
	 */
	Object get(String hql, Object... args);

	/**
	 * 封装 get 方法，根据hql查询数量
	 * 
	 * @param hql
	 *            例:count("select count(id) from User where level > ?",1)
	 * @param hql
	 *            或:count(select count(*) from User where level > ?)
	 * 
	 */
	Long count(String hql, Object... args);

	/**
	 * 封装 get 方法，根据hql查询总数
	 * 
	 * @param hql
	 *            例:sum("select sum(num) from User where level > ?",1)
	 * @param hql
	 *            或:sum("select sum(num) from User where level > 1")
	 * 
	 */
	long sum(String hql, Object... args);

	/** 封装query方法，查询表所有记录数 */
	<T> List<T> find(Class<T> clazz);

	/**
	 * 封装query方法，where 为查询条件
	 * 
	 * @param hql
	 *            例:find(User.class,"level = ? and gold = ?",1,1)
	 */
	<T> List<T> find(Class<T> clazz, String where, Object... values);

	/**
	 * query方法
	 * 
	 * @param hql
	 *            例:find("select * from User where level = ?",1);
	 * @param hql
	 *            或:find("select name,level from User where level = ?",1);
	 */
	List<Object> find(String hql, Object... args);

}
