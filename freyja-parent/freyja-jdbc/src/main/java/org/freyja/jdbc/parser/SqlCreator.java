package org.freyja.jdbc.parser;

import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.beans.BeanMap;

import org.freyja.jdbc.mapping.ShardingProperty;
import org.freyja.jdbc.object.DbResult;
import org.freyja.jdbc.object.Parameter;
import org.freyja.jdbc.sharding.FreyjaEntity;

import com.alibaba.druid.mapping.Property;

public class SqlCreator {

	public static <T> Parameter save(T t) {

		FreyjaEntity<T> entity = ShardingUtil.getEntity(t.getClass());

		List<Object> args = new ArrayList<Object>();
		Object subValue = null;

		BeanMap beanMap = BeanMap.create(t);

		for (Property p : entity.getProperties().values()) {
			ShardingProperty s = (ShardingProperty) p;
			if (s.isId() && s.isAuto()) {
				continue;
			}
			Object propertyValue = beanMap.get(p.getName());
			// Object propertyValue = PropertyUtils.getProperty(bean,
			// p.getName());
			args.add(propertyValue);
			if (s.isSubColumn()) {
				subValue = propertyValue;
			}
		}
		String sql = null;
		Parameter parameter = new Parameter(args.toArray(),
				entity.getSqlTyps("insert"));
		if (entity.isSubTable()) {

			DbResult result = entity.toInsert(subValue);
			sql = result.getSql();
			parameter.setDbNo(result.getDbNo());
		} else {
			sql = entity.toInsert();
		}
		parameter.setSql(sql);
		return parameter;
	}

	public static Parameter update(Object bean) {
		FreyjaEntity entity = ShardingUtil.getEntity(bean.getClass());
		List<Object> args = new ArrayList<Object>();
		Object idValue = null;

		BeanMap beanMap = BeanMap.create(bean);
		for (Property p : entity.getProperties().values()) {
			ShardingProperty s = (ShardingProperty) p;

			Object propertyValue = beanMap.get(p.getName());
			// Object propertyValue = PropertyUtils.getProperty(bean,
			// p.getName());
			if (s.isId()) {
				idValue = propertyValue;
				continue;
			}
			args.add(propertyValue);
		}
		args.add(idValue);

		Parameter parameter = new Parameter(args.toArray(),
				entity.getSqlTyps("update"));
		String sql = null;
		if (entity.isSubTable()) {
			DbResult result = entity.toUpdate(idValue);

			sql = result.getSql();
			parameter.setDbNo(result.getDbNo());
		} else {
			sql = entity.toUpdate();

		}
		parameter.setSql(sql);
		return parameter;
	}

	public static Parameter delete(Object bean) {

		FreyjaEntity entity = ShardingUtil.getEntity(bean.getClass());

		BeanMap beanMap = BeanMap.create(bean);

		Object idValue = beanMap.get(entity.getId().getName());

		// Object idValue = PropertyUtils.getProperty(bean, entity.getId()
		// .getName());

		Parameter parameter = new Parameter(new Object[] { idValue },
				entity.getSqlTyps("delete"));
		String sql;
		if (entity.isSubTable()) {
			DbResult result = entity.toDelete(idValue);
			sql = result.getSql();

			parameter.setDbNo(result.getDbNo());
		} else {
			sql = entity.toDelete();
		}
		parameter.setSql(sql);
		return parameter;
	}

	public static Parameter get(FreyjaEntity entity, Object idValue) {

		Parameter parameter = new Parameter(new Object[] { idValue },
				entity.getSqlTyps("select"));
		String sql;
		if (entity.isSubTable()) {
			DbResult result = entity.toSelect(idValue);

			sql = result.getSql();
			parameter.setDbNo(result.getDbNo());
		} else {
			sql = entity.toSelect();
		}
		parameter.setSql(sql);
		return parameter;
	}
}
