package org.freyja.server.bo;

import java.util.List;

import org.springframework.expression.Expression;

public class ExpressionBO {
	private Expression expression;
	private String[] names;

	private List<String> list;

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public String[] getNames() {
		return names;
	}

	public void setNames(String[] names) {
		this.names = names;
	}

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

}
