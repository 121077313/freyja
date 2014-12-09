package org.freyja.server.mina.message;

import java.util.ArrayList;
import java.util.List;

import org.freyja.server.exception.AssertCode;
import org.freyja.server.exception.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class Request extends Message {

	private static final Logger logger = LoggerFactory.getLogger(Request.class);

	public static final List<Object> emptyList = new ArrayList<Object>();

	/** 消息参数 */
	private List<Object> parameters;

	private Object parameter = emptyList;

	private byte[] bytes;

	private boolean json;

	public List<Object> getParameters() {
		return parameters;
	}

	public void setParameters(List<Object> parameters) {
		this.parameters = parameters;
	}

	/** 请求字节转化成属性 */
	public void bodyFromBytes(byte[] bytes) {
		if (bytes == null) {
			return;
		}
		try {
			parameters = (List<Object>) JSON.parse(bytes);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			AssertCode.error(ServerException.unable_resolve_msg);
		}
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public Object getParameter() {
		return parameter;
	}

	public void setParameter(Object parameter) {
		this.parameter = parameter;
	}

	public boolean isJson() {
		return json;
	}

	public void setJson(boolean json) {
		this.json = json;
	}

}
