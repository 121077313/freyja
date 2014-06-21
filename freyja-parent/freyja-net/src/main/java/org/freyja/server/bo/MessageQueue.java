package org.freyja.server.bo;

import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;

public class MessageQueue {

	private IoSession ioSession;

	private Object msg;

	public MessageQueue(IoSession ioSession, Object msg) {
		super();
		this.ioSession = ioSession;
		this.msg = msg;
	}

	public IoSession getIoSession() {
		return ioSession;
	}

	public void setIoSession(IoSession ioSession) {
		this.ioSession = ioSession;
	}

	public Object getMsg() {
		return msg;
	}

	public void setMsg(Object msg) {
		this.msg = msg;
	}

}
