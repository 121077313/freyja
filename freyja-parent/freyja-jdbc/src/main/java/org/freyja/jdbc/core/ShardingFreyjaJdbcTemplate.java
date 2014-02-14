package org.freyja.jdbc.core;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.freyja.jdbc.core.rowMapper.BeanPropertyRowMapper;
import org.freyja.jdbc.core.rowMapper.MapRowMapper;
import org.freyja.jdbc.core.rowMapper.ObjectRowMapper;
import org.freyja.jdbc.ds.DbContextHolder;
import org.freyja.jdbc.mapping.ShardingEntity;
import org.freyja.jdbc.mapping.ShardingProperty;
import org.freyja.jdbc.object.DbResult;
import org.freyja.jdbc.object.ExplainResult;
import org.freyja.jdbc.object.Parameter;
import org.freyja.jdbc.parser.ShardingUtil;
import org.freyja.jdbc.parser.SqlCreator;
import org.freyja.jdbc.sharding.FreyjaEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BatchUpdateUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.Assert;

import com.alibaba.druid.mapping.Property;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;

public class ShardingFreyjaJdbcTemplate extends FreyjaJdbcTemplate {

	@Override
	public <T> T get(Class<T> clazz, Object id) {
		Assert.notNull(id);
		FreyjaEntity<T> entity = ShardingUtil.getEntity(clazz);

		if (!entity.isSubTable()) {
			return super.get(clazz, id);
		}

		Parameter parameter = SqlCreator.get(entity, id);
		DbContextHolder.setDbNum(parameter.getDbNo());
		List<T> list = jdbcTemplate.query(parameter.getSql(),
				parameter.getArgs(), parameter.getSqlTypes(),
				new BeanPropertyRowMapper<T>(entity));

		return super.get(list);

	}

	private <T> T realSave(T t) {

		Assert.notNull(t);
		ShardingEntity entity = ShardingUtil.getEntity(t.getClass());
		Assert.notNull(entity);
		Parameter parameter = SqlCreator.save(t);
		DbContextHolder.setDbNum(parameter.getDbNo());
		if (entity.getId() == null || !entity.getId().isAuto()) {

			jdbcTemplate.update(parameter.getSql(), parameter.getArgs(),
					parameter.getSqlTypes());

			return null;
		}

		KeyHolder holder = new GeneratedKeyHolder();
		PreparedStatementCreatorFactory ps = new PreparedStatementCreatorFactory(
				parameter.getSql(), parameter.getSqlTypes());
		ps.setReturnGeneratedKeys(true);
		PreparedStatementCreator psc = ps.newPreparedStatementCreator(parameter
				.getArgs());
		jdbcTemplate.update(psc, holder);

		ShardingProperty p = entity.getId();

		PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(t.getClass(),
				p.getName());

		Method method = pd.getWriteMethod();
		method.setAccessible(true);
		try {
			method.invoke(t, holder.getKey());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return t;

	}

	@Override
	public <T> void batchSave(final List<T> list) {

		if (list == null || list.size() == 0) {
			return;
		}
		T t = list.get(0);
		Parameter parameter = SqlCreator.save(t);
		DbContextHolder.setDbNum(parameter.getDbNo());

		String sql = parameter.getSql();

		final int[] sqlTypes = parameter.getSqlTypes();

		List<Object[]> batchValues = new ArrayList<Object[]>();

		int i = 1;

		for (T tt : list) {

			Parameter p = SqlCreator.save(tt);

			// List<Object> args = new ArrayList<Object>();
			// for (Property p : entity.getProperties().values()) {
			// ShardingProperty s = (ShardingProperty) p;
			// if (s.isId() && s.isAuto()) {
			// continue;
			// }
			//
			// Object propertyValue = PropertyUtils.getProperty(tt,
			// p.getName());
			// args.add(propertyValue);
			// }
			batchValues.add(p.getArgs());

			if (i % batch_size == 0) {
				BatchUpdateUtils.executeBatchUpdate(sql, batchValues, sqlTypes,
						jdbcTemplate);
				batchValues.clear();
			}
			i++;
		}

		if (list.size() % batch_size != 0) {
			BatchUpdateUtils.executeBatchUpdate(sql, batchValues, sqlTypes,
					jdbcTemplate);
		}

	}

	@Override
	public <T> void batchUpdate(final List<T> list) {

		if (list == null || list.size() == 0) {
			return;
		}

		T t = list.get(0);
		final Parameter parameter = SqlCreator.update(t);
		DbContextHolder.setDbNum(parameter.getDbNo());

		String sql = parameter.getSql();

		final int[] sqlTypes = parameter.getSqlTypes();

		ShardingEntity entity = ShardingUtil.getEntity(t.getClass());

		List<Object[]> batchValues = new ArrayList<Object[]>();

		int i = 1;
		Collection<Property> collection = entity.getProperties().values();
		for (T tt : list) {

			Parameter p = SqlCreator.update(tt);

			// List<Object> args = new ArrayList<Object>();
			//
			// Object idValue = null;
			// for (Property p : collection) {
			// ShardingProperty s = (ShardingProperty) p;
			// Object propertyValue = PropertyUtils.getProperty(tt,
			// p.getName());
			// if (s.isId()) {
			// idValue = propertyValue;
			// continue;
			// }
			// args.add(propertyValue);
			// }
			// args.add(idValue);

			batchValues.add(p.getArgs());

			if (i % batch_size == 0) {
				BatchUpdateUtils.executeBatchUpdate(sql, batchValues, sqlTypes,
						jdbcTemplate);
				batchValues.clear();
			}
			i++;
		}

		if (list.size() % batch_size != 0) {
			BatchUpdateUtils.executeBatchUpdate(sql, batchValues, sqlTypes,
					jdbcTemplate);
		}

	}

	private final ExecutorService executors = Executors.newCachedThreadPool();

	@Override
	public <T> T save(final T t) {

		ShardingEntity entity = ShardingUtil.getEntity(t.getClass());

		if (entity.isSaveAsync()) {
			executors.execute(new Runnable() {
				@Override
				public void run() {
					realSave(t);
				}
			});

			return null;
		}

		return realSave(t);
	}

	private <T> void realUpdate(T t) {

		Assert.notNull(t);
		Parameter parameter = SqlCreator.update(t);

		DbContextHolder.setDbNum(parameter.getDbNo());
		jdbcTemplate.update(parameter.getSql(), parameter.getArgs(),
				parameter.getSqlTypes());

	}

	@Override
	public <T> void update(final T t) {

		ShardingEntity entity = ShardingUtil.getEntity(t.getClass());

		if (entity.isUpdateAsync()) {
			executors.execute(new Runnable() {
				@Override
				public void run() {
					realUpdate(t);
				}
			});
			return;
		}

		realUpdate(t);

	}

	@Override
	public <T> void delete(T t) {
		Assert.notNull(t);
		Parameter parameter = SqlCreator.delete(t);

		DbContextHolder.setDbNum(parameter.getDbNo());
		jdbcTemplate.update(parameter.getSql(), parameter.getArgs(),
				parameter.getSqlTypes());

	}

	@Override
	public void execute(String sql, Object... args) {
		Assert.notNull(sql);
		if (sql.startsWith("update")) {
			this.executeUpdate(sql, args);
		} else if (sql.startsWith("insert")) {
			this.executeInsert(sql, args);
		} else if (sql.startsWith("delete")) {
			this.executeDelete(sql, args);
		} else {
			throw new RuntimeException("error execute type");
		}
	}

	private void executeUpdate(String sql, Object... args) {

		ExplainResult result = ShardingUtil.explainToUpdateExplainResult(sql);
		if (result.isNeedSharding()) {

			List<DbResult> list = ShardingUtil.explainToUpdateSQLList(
					(SQLUpdateStatement) result.getStmt(), Arrays.asList(args));

			for (DbResult r : list) {
				DbContextHolder.setDbNum(r.getDbNo());

				jdbcTemplate.update(r.getSql(), args);
			}

		} else {
			DbContextHolder.setDbNum(-1);
			jdbcTemplate.update(result.getJdbcSql(), args);
		}

	}

	private void executeDelete(String sql, Object... args) {

		ExplainResult result = ShardingUtil.explainToDeleteExplainResult(sql);
		String shardingSql = result.getJdbcSql();
		if (result.isNeedSharding()) {
			List<DbResult> list = ShardingUtil.explainToDeleteSQLList(
					(SQLDeleteStatement) result.getStmt(), Arrays.asList(args));

			for (DbResult r : list) {
				shardingSql = r.getSql();
				DbContextHolder.setDbNum(r.getDbNo());
				jdbcTemplate.update(shardingSql, args);
			}
		} else {
			DbContextHolder.setDbNum(-1);
			jdbcTemplate.update(shardingSql, args);
		}

	}

	private void executeInsert(String sql, Object... args) {

		ExplainResult result = ShardingUtil.explainToInsertExplainResult(sql);
		String shardingSql = result.getJdbcSql();
		if (result.isNeedSharding()) {
			DbResult r = ShardingUtil.explainToInsertSQL(result.getInsert(),
					Arrays.asList(args));
			shardingSql = r.getSql();
		}
		jdbcTemplate.update(shardingSql, args);

	}

	@Override
	public List query(String sql, Object... values) {

		ExplainResult explain = ShardingUtil.explainToSelectExplainResult(sql);

		List<DbResult> results = new ArrayList<DbResult>();
		if (explain.isNeedSharding()) {
			List<DbResult> dbrs = ShardingUtil.explainToSelectSQLArray(
					explain.getQuery(), Arrays.asList(values));
			results.addAll(dbrs);
		} else {
			DbResult result = new DbResult();
			result.setSql(explain.getJdbcSql());
			results.add(result);
		}
		List list = new ArrayList();
		String tableName = explain.getTableName();

		for (DbResult result : results) {
			DbContextHolder.setDbNum(result.getDbNo());

			List l = null;
			if (tableName != null) {
				ShardingEntity entity = ShardingUtil.getEntity(tableName);
				l = jdbcTemplate.query(result.getSql(), values,
						new BeanPropertyRowMapper(entity));
			} else {
				l = jdbcTemplate.query(result.getSql(), values,
						new ObjectRowMapper());
			}
			list.addAll(l);
		}
		return list;
	}

	@Override
	public List<Map<String, Object>> queryForMap(String sql, Object... values) {

		ExplainResult explain = ShardingUtil.explainToSelectExplainResult(sql);

		List<DbResult> results = new ArrayList<DbResult>();
		if (explain.isNeedSharding()) {
			List<DbResult> dbrs = ShardingUtil.explainToSelectSQLArray(
					explain.getQuery(), Arrays.asList(values));
			results.addAll(dbrs);
		} else {
			DbResult result = new DbResult();
			result.setSql(explain.getJdbcSql());
			results.add(result);
		}
		List list = new ArrayList();
		String tableName = explain.getTableName();
		for (DbResult result : results) {
			DbContextHolder.setDbNum(result.getDbNo());

			List l = jdbcTemplate.query(result.getSql(), values,
					new MapRowMapper());
			list.addAll(l);
		}
		return list;
	}

}
