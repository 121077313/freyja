package org.freyja.data.dao.impl;

import java.util.List;
import java.util.Map;

import org.freyja.data.dao.ICommonDao;
import org.freyja.jdbc.core.FreyjaJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CommonDaoImpl implements ICommonDao {

	@Autowired
	private FreyjaJdbcTemplate freyjaTemplate;

	@Override
	public Object get(String hql, Object... args) {
		List<Object> list = freyjaTemplate.query(hql, args);
		if (list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public List find(String hql, Object... args) {
		return freyjaTemplate.query(hql, args);
	}

	@Override
	public <T> List<T> find(Class<T> clazz) {
		return freyjaTemplate.query("select * from " + clazz.getSimpleName());
	}

	@Override
	public <T> List<T> find(Class<T> clazz, String where, Object... values) {
		List<T> list = freyjaTemplate.query("select * from " + clazz.getSimpleName()
				+ " where " + where, values);
		return list;
	}

	@Override
	public <T> T get(Class<T> clazz, String where, Object... values) {
		List list = this.find(clazz, where, values);
		if (list.size() == 0) {
			return null;
		}
		return (T) list.get(0);
	}

	@Override
	public long count(String hql, Object... args) {
		Object obj = this.get(hql, args);
		return (Long) obj;
	}

	@Override
	public long sum(String hql, Object... args) {
		Object obj = this.get(hql, args);

		if (obj == null) {
			return 0;
		}
		return ((Long) obj).longValue();
	}

	@Override
	public <T> T get(Class<T> clazz, Object id) {
		return freyjaTemplate.get(clazz, id);
	}

	@Override
	public <T> T save(T t) {
		return freyjaTemplate.save(t);
	}

	@Override
	public <T> void update(T t) {
		freyjaTemplate.update(t);
	}

	@Override
	public <T> void delete(T t) {
		freyjaTemplate.delete(t);
	}

	@Override
	public <T> void batchSave(List<T> list) {
		freyjaTemplate.batchSave(list);
	}

	@Override
	public <T> void batchUpdate(List<T> list) {
		freyjaTemplate.batchUpdate(list);
	}

	@Override
	public void execute(String sql, Object... args) {
		freyjaTemplate.execute(sql, args);
	}

	@Override
	public List<Object> query(String sql, Object... args) {
		return freyjaTemplate.query(sql, args);
	}

	@Override
	public List<Map<String, Object>> queryForMap(String sql, Object... args) {
		return freyjaTemplate.queryForMap(sql, args);
	}

}