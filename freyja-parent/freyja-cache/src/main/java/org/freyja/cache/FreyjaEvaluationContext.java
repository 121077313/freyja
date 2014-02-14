package org.freyja.cache;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;

public class FreyjaEvaluationContext extends StandardEvaluationContext {

	private final ParameterNameDiscoverer paramDiscoverer;

	private final Method method;

	private final Object[] args;
	private final Object returnValue;

	private final Class<?> targetClass;

	private final Map<String, Method> methodCache;

	private boolean paramLoaded = false;

	public FreyjaEvaluationContext(Object rootObject,
			ParameterNameDiscoverer paramDiscoverer, Method method,
			Object[] args, Object returnValue, Class<?> targetClass,
			Map<String, Method> methodCache) {
		if (returnValue != null) {
			setRootObject(returnValue);
		} else {
			setRootObject(rootObject);
		}

		this.paramDiscoverer = paramDiscoverer;
		this.method = method;
		this.args = args;
		this.returnValue = returnValue;
		this.targetClass = targetClass;
		this.methodCache = methodCache;
	}

	/**
	 * Load the param information only when needed.
	 */
	@Override
	public Object lookupVariable(String name) {
		Object variable = super.lookupVariable(name);
		if (variable != null) {
			return variable;
		}
		if (!this.paramLoaded) {
			loadArgsAsVariables();
			// loadReturnValueAsVariables();
			this.paramLoaded = true;
			variable = super.lookupVariable(name);
		}
		return variable;
	}

	private void loadReturnValueAsVariables() {
		if (returnValue == null) {
			return;
		}
		setRootObject(returnValue);

	}

	private void loadArgsAsVariables() {
		// shortcut if no args need to be loaded
		if (ObjectUtils.isEmpty(this.args)) {
			return;
		}

		String mKey = toString(this.method);
		Method targetMethod = this.methodCache.get(mKey);
		if (targetMethod == null) {
			targetMethod = AopUtils.getMostSpecificMethod(this.method,
					this.targetClass);
			if (targetMethod == null) {
				targetMethod = this.method;
			}
			this.methodCache.put(mKey, targetMethod);
		}

		// save arguments as indexed variables
		for (int i = 0; i < this.args.length; i++) {
			setVariable("a" + i, this.args[i]);
			setVariable("p" + i, this.args[i]);
		}

		String[] parameterNames = this.paramDiscoverer
				.getParameterNames(targetMethod);
		// save parameter names (if discovered)
		if (parameterNames != null) {
			for (int i = 0; i < parameterNames.length; i++) {
				setVariable(parameterNames[i], this.args[i]);
			}
		}
	}

	private String toString(Method m) {
		StringBuilder sb = new StringBuilder();
		sb.append(m.getDeclaringClass().getName());
		sb.append("#");
		sb.append(m.toString());
		return sb.toString();
	}

}
