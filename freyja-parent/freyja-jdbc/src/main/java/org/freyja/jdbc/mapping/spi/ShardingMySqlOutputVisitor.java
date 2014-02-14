package org.freyja.jdbc.mapping.spi;

import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;

public class ShardingMySqlOutputVisitor extends MySqlOutputVisitor {

	public ShardingMySqlOutputVisitor(Appendable appender) {
		super(appender);
	}

	@Override
	public void println() {
		print(" ");
		printIndent();
	}

	@Override
	public void printIndent() {
		// super.printIndent();
	}

//	@Override
//	public boolean visit(SQLSelectItem x) {
//		x.getExpr().accept(this);
//
//		return false;
//	}

//	@Override
//	public boolean visit(SQLCastExpr x) {
//		print("CAST(");
//		x.getExpr().accept(this);
//		x.getDataType().accept(this);
//		print(")");
//
//		return false;
//	}
}
