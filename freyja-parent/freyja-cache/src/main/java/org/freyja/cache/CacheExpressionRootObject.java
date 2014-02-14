package org.freyja.cache;

import java.lang.reflect.Method;

import org.springframework.util.Assert;

public class CacheExpressionRootObject {

	private final Method method;

	private final Object[] args;
	
	private final Object returnValue;

	private final Object target;

	private final Class<?> targetClass;

	public CacheExpressionRootObject(Method method, Object[] args,Object returnValue,
			Object target, Class<?> targetClass) {

		Assert.notNull(method, "Method is required");
		Assert.notNull(targetClass, "targetClass is required");
		this.method = method;
		this.target = target;
		this.targetClass = targetClass;
		this.args = args;
		this.returnValue=returnValue;
	}

	public Method getMethod() {
		return this.method;
	}

	public String getMethodName() {
		return this.method.getName();
	}

	public Object[] getArgs() {
		return this.args;
	}

	public Object getTarget() {
		return this.target;
	}

	public Class<?> getTargetClass() {
		return this.targetClass;
	}

	public Object getReturnValue() {
		return returnValue;
	}

}
