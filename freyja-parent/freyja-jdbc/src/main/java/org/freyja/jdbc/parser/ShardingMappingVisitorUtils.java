package org.freyja.jdbc.parser;

import java.util.Map;

import com.alibaba.druid.mapping.Entity;
import com.alibaba.druid.mapping.Property;
import com.alibaba.druid.mapping.spi.MappingVisitor;
import com.alibaba.druid.mapping.spi.PropertyValue;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;

public class ShardingMappingVisitorUtils {
	
    private static final String MAPPING_VAR_INDEX = "mapping.varIndex";
    private static final String MAPPING_VALUE     = "mapping.value";
    private static final String MAPPING_PROPERTY  = "mapping.property";
    private static final String MAPPING_ENTITY    = "mapping.entity";

	public static boolean visit(MappingVisitor visitor,
			SQLBinaryOpExpr x) {
		x.getLeft().setParent(x);
		x.getRight().setParent(x);

		if (x.getOperator() == SQLBinaryOperator.Equality) {
			if (x.getLeft() instanceof SQLIdentifierExpr
					&& isSimpleValue(visitor, x.getRight())) {
//				visit(visitor, (SQLIdentifierExpr) x.getLeft());
//				x.getRight().accept(visitor);

				Entity entity = (Entity) x.getLeft().getAttribute(
						MAPPING_ENTITY);
				Property property = (Property) x.getLeft().getAttribute(
						MAPPING_PROPERTY);
				Object value = x.getRight().getAttribute(MAPPING_VALUE);

				PropertyValue propertyValue = new PropertyValue(entity,
						property, value);
				propertyValue.putAttribute("mapping.expr", x.getRight());

				visitor.getPropertyValues().add(propertyValue);

				return false;
			}

			if (x.getLeft() instanceof SQLPropertyExpr
					&& isSimpleValue(visitor, x.getRight())) {
//				visit(visitor, (SQLPropertyExpr) x.getLeft());
//				x.getRight().accept(visitor);

				Entity entity = (Entity) x.getLeft().getAttribute(
						MAPPING_ENTITY);
				Property property = (Property) x.getLeft().getAttribute(
						MAPPING_PROPERTY);
				Object value = x.getRight().getAttribute(MAPPING_VALUE);

				PropertyValue propertyValue = new PropertyValue(entity,
						property, value);
				propertyValue.putAttribute("mapping.expr", x.getRight());

				visitor.getPropertyValues().add(propertyValue);

				return false;
			}
		}

		return true;
	}

	private static boolean isSimpleValue(MappingVisitor visitor, SQLExpr expr) {
		if (expr instanceof SQLNumericLiteralExpr) {
			expr.putAttribute(MAPPING_VALUE,
					((SQLNumericLiteralExpr) expr).getNumber());
			return true;
		}

		if (expr instanceof SQLCharExpr) {
			expr.putAttribute(MAPPING_VALUE, ((SQLCharExpr) expr).getText());
			return true;
		}

		if (expr instanceof SQLVariantRefExpr) {
			Map<String, Object> attributes = expr.getAttributes();
			Integer varIndex = (Integer) attributes.get(MAPPING_VAR_INDEX);
			if (varIndex == null) {
				varIndex = visitor.getAndIncrementVariantIndex();
				expr.putAttribute(MAPPING_VAR_INDEX, varIndex);
			}

			if (visitor.getParameters().size() > varIndex) {
				Object parameter = visitor.getParameters().get(varIndex);
				expr.putAttribute(MAPPING_VALUE, parameter);
			}

			return true;
		}

		return false;
	}
}
