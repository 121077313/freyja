package spring;

import java.lang.reflect.Method;

import org.junit.runner.RunWith;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

public class SpringSpelTest {
	public static void main(String[] args) throws SecurityException,
			NoSuchMethodException {
		a(1);
	}

	private static ParameterNameDiscoverer paramNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	public static void a(Integer a) throws SecurityException,
			NoSuchMethodException {
		Req req = new Req();
		req.setName("");
		req.setUid(23);
		
		
//		Method method = SpringSpelTest.class.getMethod("a", Integer.class);
//		String[] names = paramNameDiscoverer.getParameterNames(method);
		StandardEvaluationContext context = new StandardEvaluationContext(req);
		ExpressionParser parser = new SpelExpressionParser();
		Expression expression = parser.parseExpression("{uid,name,#uid2}");
		context.setVariable("uid2", 1);
		Object[] playing = expression.getValue(context, Object[].class);
		System.out.println();

	}

}

class Req {

	private int uid;
	
	private String name;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
