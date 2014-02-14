package org.freyja.jdbc.parser;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.freyja.jdbc.annotation.Async;
import org.freyja.jdbc.annotation.Column;
import org.freyja.jdbc.annotation.Id;
import org.freyja.jdbc.annotation.SubColumn;
import org.freyja.jdbc.annotation.Table;
import org.freyja.jdbc.annotation.Transient;
import org.freyja.jdbc.mapping.ShardingEntity;
import org.freyja.jdbc.mapping.ShardingMappingEngine;
import org.freyja.jdbc.mapping.ShardingProperty;
import org.freyja.jdbc.mapping.spi.TableMySqlMappingVisitor;
import org.freyja.jdbc.object.DbResult;
import org.freyja.jdbc.object.ExplainResult;
import org.freyja.jdbc.sharding.FreyjaEntity;
import org.freyja.jdbc.sharding.ShardingStrategy;
import org.springframework.util.Assert;

import com.alibaba.druid.mapping.MappingContext;
import com.alibaba.druid.mapping.spi.MappingVisitor;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;

public class ShardingUtil {

	private static Map<String, ExplainResult> explainCache = new ConcurrentHashMap<String, ExplainResult>();

	public static ShardingMappingEngine engine = new ShardingMappingEngine();

	public static <T> FreyjaEntity<T> getEntity(String tableName) {
		return (FreyjaEntity) engine.getEntities().get(tableName);
	}

	public static <T> FreyjaEntity<T> getEntity(Class clazz) {
		return (FreyjaEntity<T>) engine.getEntities()
				.get(clazz.getSimpleName());
	}

	public static <T> void createEntity(Class<T> clazz) {
		FreyjaEntity<T> entity = new FreyjaEntity<T>();

		Table table = clazz.getAnnotation(Table.class);

		entity.setName(clazz.getSimpleName());
		entity.setTableName(table.name());
		entity.setSubTable(table.isSubTable());
		entity.setClazz(clazz);

		Async async = clazz.getAnnotation(Async.class);
		if (async != null) {
			entity.setSaveAsync(async.saveAsync());
			entity.setUpdateAsync(async.updateAsync());
		}

		Field[] fields = clazz.getDeclaredFields();

		ShardingProperty idProperty = null;
		for (Field field : fields) {
			PropertyDescriptor pd = org.springframework.beans.BeanUtils
					.getPropertyDescriptor(clazz, field.getName());

			if (pd == null || pd.getWriteMethod() == null
					|| pd.getReadMethod() == null) {
				continue;
			}

			if (field.isAnnotationPresent(Transient.class)) {
				continue;
			}

			String columnName = field.getName();
			Column column = field.getAnnotation(Column.class);
			if (column != null) {
				columnName = column.name();
			}

			ShardingProperty property = new ShardingProperty(field.getType(),
					field.getName(), columnName);

			if (field.isAnnotationPresent(Id.class)) {
				property.setId(true);
				Id id = field.getAnnotation(Id.class);
				property.setAuto(id.auto());
				idProperty = property;
			}
			if (entity.isSubTable()) {
				if (field.isAnnotationPresent(SubColumn.class)) {
					property.setSubColumn(true);
				}
			}

			entity.addProperty(property);

		}

		Assert.notNull(idProperty,"没有配置Id注解"+clazz.getName());

		engine.getEntities().put(table.name(), entity);
		engine.addEntity(entity);
	}

	public static List<DbResult> explainToSelectSQLArray(
			SQLSelectQueryBlock query, List<Object> parameters) {
		// ShardingMySqlMappingProvider provider = new
		// ShardingMySqlMappingProvider();

		// MappingVisitor visitor = provider.createMappingVisitor(engine,
		// new MappingContext(parameters));

		TableMySqlMappingVisitor visitor = new TableMySqlMappingVisitor(engine,
				new MappingContext(parameters));
		query.accept(visitor);

		DbResult f = engine.shardingAfterResole(visitor);
		List<DbResult> results = new ArrayList<DbResult>();
		if (f.isNeedCycle()) {
			for (int j = 0; j < engine.getShardingStrategy().getDbNum(); j++) {
				for (int i = 0; i < engine.getShardingStrategy().getTableNum(); i++) {
					DbResult result = new DbResult();
					for (SQLTableSource tableSource : visitor.getTableSources()
							.values()) {
						FreyjaEntity entity = (FreyjaEntity) tableSource
								.getAttribute("mapping.entity");
						if (entity == null) {
							continue;
						}
						String shardingTableName = engine.getShardingStrategy()
								.getShardingTableName(entity.getTableName(), i)
								.getTableName();

						SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
						exprTableSource.setExpr(new SQLIdentifierExpr(
								shardingTableName));
					}
					result.setSql(engine.toSQL(query));
					result.setDbNo(j);
					result.setTableNo(i);
					results.add(result);
				}
			}
		} else {
			f.setSql(engine.toSQL(query));
			results.add(f);
		}
		return results;
	}

	public static ExplainResult explainToSelectExplainResult(String sql) {
		ExplainResult result = explainCache.get(sql);
		if (result != null) {
			return result;
		}

		SQLSelectQueryBlock query = engine.explainToSelectSQLObject(sql);
		MappingVisitor visitor = engine.createMappingVisitor();
		query.accept(visitor);

		visitor.afterResolve();

		String jdbcSql = engine.toSQL(query);

		boolean needSharding = false;
		for (SQLTableSource tableSource : visitor.getTableSources().values()) {
			ShardingEntity entity = (ShardingEntity) tableSource
					.getAttribute("mapping.entity");

			if (entity == null) {
				continue;
			}
			if (entity.isSubTable()) {
				needSharding = true;
				break;
			}
		}

		String tableName = FreyjaUtil.isSingle(query);

		result = new ExplainResult(jdbcSql, tableName, needSharding, query);
		explainCache.put(sql, result);
		return result;
	}

	public static ExplainResult explainToUpdateExplainResult(String sql) {

		ExplainResult result = explainCache.get(sql);
		if (result != null) {
			return result;
		}

		SQLUpdateStatement stmt = engine.explainToUpdateSQLObject(sql,
				new MappingContext());

		String tableName = stmt.getTableName().getSimleName();

		MappingVisitor visitor = engine.createMappingVisitor(Collections
				.emptyList());
		stmt.accept(visitor);
		visitor.afterResolve();

		String jdbcSql = engine.toSQL(stmt);

		ShardingEntity entity = ShardingUtil.getEntity(tableName);

		result = new ExplainResult(jdbcSql, tableName, entity.isSubTable(),
				stmt);
		return result;
	}

	public static ExplainResult explainToDeleteExplainResult(String sql) {

		ExplainResult result = explainCache.get(sql);
		if (result != null) {
			return result;
		}

		SQLDeleteStatement stmt = engine.explainToDeleteSQLObject(sql,
				new MappingContext());

		String tableName = stmt.getTableName().getSimleName();

		MappingVisitor visitor = engine.createMappingVisitor(Collections
				.emptyList());
		stmt.accept(visitor);
		visitor.afterResolve();
		String jdbcSql = engine.toSQL(stmt);
		ShardingEntity entity = ShardingUtil.getEntity(tableName);

		result = new ExplainResult(jdbcSql, tableName, entity.isSubTable(),
				stmt);
		return result;
	}

	public static ExplainResult explainToInsertExplainResult(String sql) {

		ExplainResult result = explainCache.get(sql);
		if (result != null) {
			return result;
		}

		SQLInsertStatement stmt = engine.explainToInsertSQLObject(sql,
				new MappingContext());

		String tableName = stmt.getTableName().getSimleName();
		MappingVisitor visitor = engine.createMappingVisitor();
		stmt.accept(visitor);
		visitor.afterResolve();
		String jdbcSql = engine.toSQL(stmt);
		ShardingEntity entity = ShardingUtil.getEntity(tableName);

		result = new ExplainResult(jdbcSql, tableName, entity.isSubTable(),
				stmt);
		return result;
	}

	public static DbResult explainToUpdateSQL(SQLUpdateStatement stmt,
			List<Object> parameters) {

		// SQLUpdateStatement stmt = explainToUpdateSQLObject(sql);

		MappingVisitor visitor = engine.createMappingVisitor(parameters);
		stmt.accept(visitor);
		visitor.afterResolve();
		DbResult result = engine.shardingAfterResole(visitor);

		result.setSql(engine.toSQL(stmt));
		return result;
	}

	public static List<DbResult> explainToUpdateSQLList(
			SQLUpdateStatement stmt, List<Object> parameters) {
		TableMySqlMappingVisitor visitor = new TableMySqlMappingVisitor(engine,
				new MappingContext(parameters));
		stmt.accept(visitor);

		DbResult f = engine.shardingAfterResole(visitor);
		List<DbResult> results = new ArrayList<DbResult>();
		if (f.isNeedCycle()) {
			for (int j = 0; j < engine.getShardingStrategy().getDbNum(); j++) {
				for (int i = 0; i < engine.getShardingStrategy().getTableNum(); i++) {
					DbResult result = new DbResult();
					for (SQLTableSource tableSource : visitor.getTableSources()
							.values()) {
						FreyjaEntity entity = (FreyjaEntity) tableSource
								.getAttribute("mapping.entity");
						if (entity == null) {
							continue;
						}
						String shardingTableName = engine.getShardingStrategy()
								.getShardingTableName(entity.getTableName(), i)
								.getTableName();

						SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
						exprTableSource.setExpr(new SQLIdentifierExpr(
								shardingTableName));
					}
					result.setSql(engine.toSQL(stmt));
					result.setDbNo(j);
					result.setTableNo(i);
					results.add(result);
				}
			}
		} else {
			f.setSql(engine.toSQL(stmt));
			results.add(f);
		}
		return results;

	}

	public static List<DbResult> explainToDeleteSQLList(
			SQLDeleteStatement stmt, List<Object> parameters) {
		MappingVisitor visitor = engine.createMappingVisitor(parameters);
		stmt.accept(visitor);
		visitor.afterResolve();
		DbResult f = engine.shardingAfterResole(visitor);

		List<DbResult> results = new ArrayList<DbResult>();
		if (f.isNeedCycle()) {
			for (int j = 0; j < engine.getShardingStrategy().getDbNum(); j++) {
				for (int i = 0; i < engine.getShardingStrategy().getTableNum(); i++) {
					DbResult result = new DbResult();
					for (SQLTableSource tableSource : visitor.getTableSources()
							.values()) {
						FreyjaEntity entity = (FreyjaEntity) tableSource
								.getAttribute("mapping.entity");
						if (entity == null) {
							continue;
						}
						String shardingTableName = engine.getShardingStrategy()
								.getShardingTableName(entity.getTableName(), i)
								.getTableName();

						SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
						exprTableSource.setExpr(new SQLIdentifierExpr(
								shardingTableName));
					}
					result.setSql(engine.toSQL(stmt));
					result.setDbNo(j);
					result.setTableNo(i);
					results.add(result);
				}
			}
		} else {
			f.setSql(engine.toSQL(stmt));
			results.add(f);
		}

		return results;
	}

	public static DbResult explainToDeleteSQL(SQLDeleteStatement stmt,
			List<Object> parameters) {
		MappingVisitor visitor = engine.createMappingVisitor(parameters);
		stmt.accept(visitor);
		DbResult r = engine.shardingAfterResole(visitor);

		r.setSql(engine.toSQL(stmt));
		return r;
	}

	public static DbResult explainToInsertSQL(SQLInsertStatement stmt,
			List<Object> parameters) {

		// SQLInsertStatement stmt = explainToInsertSQLObject(sql);

		MappingVisitor visitor = engine.createMappingVisitor(parameters);
		stmt.accept(visitor);
		visitor.afterResolve();
		DbResult r = engine.shardingAfterResole(visitor);

		r.setSql(engine.toSQL(stmt));
		return r;
	}

}
