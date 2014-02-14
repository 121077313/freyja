package org.freyja.server.bo;

import org.apache.mina.core.session.IoSession;

public interface BeanInvoker<T> {

	public T invoke(Integer uid, IoSession session);

	/** 获取参数名称(用于protobuf) */
	public String getName();

}
