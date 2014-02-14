package org.freyja.server.spring.datasource;

import org.freyja.jdbc.ds.DbContextHolder;
import org.freyja.jdbc.parser.ShardingUtil;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {

		Integer dbNo = DbContextHolder.getDbNum();
//		DbContextHolder.setDbNum(-1);
		return dbNo;
	}

}