package org.freyja.jdbc.core;

import java.io.IOException;
import java.util.Properties;

import org.freyja.jdbc.parser.ShardingUtil;
import org.freyja.jdbc.sharding.FreyjaShardingStrategy;
import org.freyja.jdbc.sharding.ShardingStrategy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ClassUtils;

public abstract class FreyjaJdbcAccessor implements InitializingBean {
	private String[] packagesToScan;
	private Properties freyjaProperties;

	protected JdbcTemplate jdbcTemplate;

	protected ShardingStrategy shardingStrategy;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private static final String RESOURCE_PATTERN = "/**/*.class";

	protected void packagesToScan(String[] packagesToScan) throws Exception {
		if (packagesToScan != null) {
			for (String pkg : packagesToScan) {
				String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
						+ ClassUtils.convertClassNameToResourcePath(pkg)
						+ RESOURCE_PATTERN;
				Resource[] resources = this.resourcePatternResolver
						.getResources(pattern);
				MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(
						this.resourcePatternResolver);

				for (Resource resource : resources) {
					if (resource.isReadable()) {
						MetadataReader reader = readerFactory
								.getMetadataReader(resource);
						String className = reader.getClassMetadata()
								.getClassName();
						if (matchesFilter(reader, readerFactory)) {
							Class<?> clazz = this.resourcePatternResolver
									.getClassLoader().loadClass(className);

							ShardingUtil.createEntity(clazz);
						}
					}
				}
			}
		}
	}

	private TypeFilter[] entityTypeFilters = new TypeFilter[] { new AnnotationTypeFilter(
			org.freyja.jdbc.annotation.Table.class, false) };

	private boolean matchesFilter(MetadataReader reader,
			MetadataReaderFactory readerFactory) throws IOException {
		if (this.entityTypeFilters != null) {
			for (TypeFilter filter : this.entityTypeFilters) {
				if (filter.match(reader, readerFactory)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean show_sql = false;

	public static int batch_size;

	@Override
	public void afterPropertiesSet() throws Exception {

		// int dbNum = Integer.parseInt(freyjaProperties
		// .getProperty("db_num", "0"));
		// int tableNum = Integer.parseInt(freyjaProperties.getProperty(
		// "table_num", "0"));
		// int idSubNum = Integer.parseInt(freyjaProperties.getProperty(
		// "id_sub_num", "0"));

		show_sql = Boolean.parseBoolean(freyjaProperties.getProperty(
				"show_sql", "false"));
		batch_size = Integer.parseInt(freyjaProperties.getProperty(
				"batch_size", "200"));
		// ShardingUtil.engine.setDbNum(dbNum);
		// ShardingUtil.engine.setTableNum(tableNum);
		// ShardingUtil.engine.setIdSubNum(idSubNum);

		if (shardingStrategy == null) {

			shardingStrategy = new FreyjaShardingStrategy();
		}
		
		ShardingUtil.engine.setShardingStrategy(shardingStrategy);
		packagesToScan(packagesToScan);

	}

	public Properties getFreyjaProperties() {
		return freyjaProperties;
	}

	public void setFreyjaProperties(Properties freyjaProperties) {
		this.freyjaProperties = freyjaProperties;
	}

	public String[] getPackagesToScan() {
		return packagesToScan;
	}

	public void setPackagesToScan(String[] packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	public ShardingStrategy getShardingStrategy() {
		return shardingStrategy;
	}

	public void setShardingStrategy(ShardingStrategy shardingStrategy) {
		this.shardingStrategy = shardingStrategy;
	}

}
