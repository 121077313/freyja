package org.freyja.server.mina.message;

import com.alibaba.fastjson.annotation.JSONField;

public class Message {

	/** 消息代号 */
	private Integer cmd;

	private String cmdString;

	public Integer getCmd() {
		return cmd;
	}

	public void setCmd(Integer cmd) {
		this.cmd = cmd;
	}

	public String getCmdString() {
		return cmdString;
	}

	public void setCmdString(String cmdString) {
		this.cmdString = cmdString;
	}

}
