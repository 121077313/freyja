package org.freyja.server.mina;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.freyja.log.Log;
import org.freyja.server.bo.Forward;
import org.freyja.server.bo.MethodCache;
import org.freyja.server.bo.ResponseVO;
import org.freyja.server.core.CmdInit;
import org.freyja.server.core.Invoker;
import org.freyja.server.core.ListenerInit;
import org.freyja.server.core.SessionAware;
import org.freyja.server.exception.AssertCode;
import org.freyja.server.exception.CodeException;
import org.freyja.server.exception.ServerException;
import org.freyja.server.listener.SessionListener;
import org.freyja.server.mina.message.Request;
import org.freyja.server.mina.message.Response;
import org.freyja.server.util.RequestParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

@Component
public class InternalHandler extends IoHandlerAdapter {

	@Autowired
	private Invoker beanInvoker;

	private static Logger logger = LoggerFactory
			.getLogger(InternalHandler.class);

	@Autowired
	private SessionAware sessionAware;

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {

		Forward forward = JSON.parseObject(message.toString(), Forward.class);

		if (forward.getCmd().equals("c")) {
			sessionAware.close(forward.getUid());
		} else if (forward.getCmd().equals("f")) {
			sessionAware.write(forward.getUid(), forward.getResp());
		}
	}

}
