package org.freyja.server.bo;

import org.freyja.server.mina.message.Response;

/** 转发消息 */
public class Forward {
	
	/** 命令 */
	private String cmd;

	/** uid */
	private int uid;

	/** 内容 */
	private Response resp;

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public Response getResp() {
		return resp;
	}

	public void setResp(Response resp) {
		this.resp = resp;
	}

}
