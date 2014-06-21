package org.freyja.server.bo;

import org.freyja.server.mina.message.Response;

import com.alibaba.fastjson.JSON;

public class ResponseVO {

	/** 返回值 */
	private Object r;

	/** 请求状态，0表示正常 其他值表示出现异常 */
	private Integer s;

	private Integer cmdId;

	private String c;

	public ResponseVO(Response resp) {
		c = resp.getCmdString();
		if (c == null) {
			cmdId = resp.getCmd();
		}

		if (resp.getStatus() != 0) {
			s = resp.getStatus();
		}

		r = resp.getResult();
	}

	public Integer getCmdId() {
		return cmdId;
	}

	public void setCmdId(Integer cmdId) {
		this.cmdId = cmdId;
	}

	public Integer getS() {
		return s;
	}

	public void setS(Integer s) {
		this.s = s;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public Object getR() {
		return r;
	}

	public void setR(Object r) {
		this.r = r;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
