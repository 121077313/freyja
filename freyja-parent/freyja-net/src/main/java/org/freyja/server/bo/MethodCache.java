package org.freyja.server.bo;

import java.lang.reflect.Method;

public class MethodCache {

	private String serviceName;

	private String methodName;

	private int cmd;

	private String cmdString;

	private Method method;

	private Object service;

	private boolean hasReturn;

	public MethodCache(String serviceName, Method method, Object service,
			boolean hasReturn) {

		this.serviceName = serviceName;
		this.method = method;
		this.methodName = method.getName();
		this.service = service;
		this.cmdString = serviceName + "." + methodName;
		this.cmd = cmdString.hashCode();
		this.hasReturn = hasReturn;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getService() {
		return service;
	}

	public void setService(Object service) {
		this.service = service;
	}

	public int getCmd() {
		return cmd;
	}

	public void setCmd(int cmd) {
		this.cmd = cmd;
	}

	public String getCmdString() {
		return cmdString;
	}

	public void setCmdString(String cmdString) {
		this.cmdString = cmdString;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Service:").append(this.serviceName);
		sb.append(",Method:").append(this.methodName);
		sb.append(",cmd:").append(this.cmd);
		sb.append(",cmdString:").append(this.cmdString);

		return sb.toString();
	}

	public boolean isHasReturn() {
		return hasReturn;
	}

	public void setHasReturn(boolean hasReturn) {
		this.hasReturn = hasReturn;
	}

}
