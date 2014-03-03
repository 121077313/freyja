package org.freyja.server.mina.message;

import com.alibaba.fastjson.JSON;

public class Response extends Message {

	/** 返回值 */
	private Object result;

	/** 请求状态，0表示正常 其他值表示出现异常 */
	private int status;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	/** 转化成key */
	public String toKey() {
		return new StringBuffer().append(getCmd()).append(".")
				.append(getStatus()).toString();
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
