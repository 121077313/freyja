package org.freyja.jdbc.mapping.spi;

import com.alibaba.druid.mapping.MappingContext;
import com.alibaba.druid.mapping.MappingEngine;
import com.alibaba.druid.mapping.spi.MappingVisitor;
import com.alibaba.druid.mapping.spi.MySqlMappingProvider;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;

public class ShardingMySqlMappingProvider extends MySqlMappingProvider {

	@Override
	public SQLASTOutputVisitor createOutputVisitor(MappingEngine engine,
			Appendable out) {
		return new ShardingMySqlOutputVisitor(out);
	}

	@Override
	public MappingVisitor createMappingVisitor(MappingEngine engine) {
		return new ShardingMySqlMappingVisitor(engine);
	}

	@Override
	public MappingVisitor createMappingVisitor(MappingEngine engine,
			MappingContext context) {
		return new ShardingMySqlMappingVisitor(engine, context);
	}
}