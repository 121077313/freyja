package org.freyja.jdbc.core;

import java.util.List;
import java.util.Map;

public interface FreyjaJdbcOperations {

	public <T> T get(Class<T> clazz, Object id);

	public <T> T save(T t);

	public <T> void batchSave(List<T> list);

	public <T> void update(T t);

	public <T> void batchUpdate(List<T> list);

	public <T> void delete(T t);

	public void execute(String sql, Object... args);

	public List<?> query(String sql, Object... args);

	public List<Map<String, Object>> queryForMap(String sql, Object... args);

}
