package org.freyja.jdbc.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;

public class FreyjaUtil {

	public static boolean allColumn(SQLSelectQueryBlock query) {

		boolean allColumn = false;
		if (query.getSelectList().size() == 1) {
			if (query.getSelectList().get(0).getExpr() instanceof SQLAllColumnExpr) {
				allColumn = true;
			}
		}

		return allColumn;
	}

	public static String getTableName(SQLExprTableSource tableSource) {
		String tableName = null;
		SQLExpr expr = tableSource.getExpr();
		if (expr instanceof SQLIdentifierExpr) {
			SQLIdentifierExpr identExpr = (SQLIdentifierExpr) expr;
			tableName = identExpr.getName();

		} else if (expr instanceof SQLPropertyExpr) {
			SQLPropertyExpr proExpr = (SQLPropertyExpr) expr;
			tableName = proExpr.getName();
		}
		return tableName;
	}

	public static String isSingle(SQLSelectQueryBlock query) {
		if (query.getFrom() instanceof SQLExprTableSource) {

			String tableName = FreyjaUtil
					.getTableName((SQLExprTableSource) query.getFrom());

			if (FreyjaUtil.allColumn(query)) {
				return tableName;
			}
		}
		return null;

	}

	public static String needSharding(SQLSelectQueryBlock query) {
		if (query.getFrom() instanceof SQLExprTableSource) {

			String tableName = FreyjaUtil
					.getTableName((SQLExprTableSource) query.getFrom());

			if (FreyjaUtil.allColumn(query)) {
				return tableName;
			}
		}
		return null;

	}

}
