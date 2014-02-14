package org.freyja.jdbc.mapping.spi;


import com.alibaba.druid.mapping.MappingContext;
import com.alibaba.druid.mapping.MappingEngine;
import com.alibaba.druid.mapping.spi.MySqlMappingVisitor;

public class ShardingMySqlMappingVisitor extends MySqlMappingVisitor {
	public ShardingMySqlMappingVisitor(MappingEngine engine) {
		super(engine);
	}

	public ShardingMySqlMappingVisitor(MappingEngine engine,
			MappingContext context) {
		super(engine, context);
	}

//	@Override
//	public boolean visit(MySqlSelectQueryBlock x) {
//
//		return ShardingMappingVisitorUtils.visit(this, x);
//
//	}
}
