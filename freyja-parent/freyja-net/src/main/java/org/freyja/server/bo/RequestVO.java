package org.freyja.server.bo;

import java.util.List;

import org.freyja.server.mina.message.Request;

import com.alibaba.fastjson.JSON;

public class RequestVO {

	private String c;

	private Integer cmdId;

	private Object p;

	private List<Object> ps;

	public RequestVO(Request req) {
		c = req.getCmdString();
		if (c == null) {
			cmdId = req.getCmd();
		}
		p = req.getParameter();

		ps = req.getParameters();
		if (ps != null && ps.size() == 0) {
			ps = null;
		}
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public Integer getCmdId() {
		return cmdId;
	}

	public void setCmdId(Integer cmdId) {
		this.cmdId = cmdId;
	}

	public Object getP() {
		return p;
	}

	public void setP(Object p) {
		this.p = p;
	}

	public List<Object> getPs() {
		return ps;
	}

	public void setPs(List<Object> ps) {
		this.ps = ps;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
