package org.freyja.server.mina;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.freyja.log.Log;
import org.freyja.server.bo.MessageQueue;
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
import org.freyja.server.thread.NamedThreadFactory;
import org.freyja.server.util.RequestParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerHandler extends IoHandlerAdapter {

	@Autowired
	private Invoker beanInvoker;

	private static Logger logger = LoggerFactory.getLogger(ServerHandler.class);
	private static Logger errLogger = LoggerFactory.getLogger("exception");

	@Autowired
	private SessionAware sessionAware;

	@Autowired
	private ListenerInit listenerInitialization;

	/** 方法线程池 */
	public static ExecutorService methodScheduler = Executors
			.newFixedThreadPool(50, new NamedThreadFactory("method"));

	// Queue<MessageQueue> queue = new ConcurrentLinkedQueue<MessageQueue>();

	Map<Integer, Queue<MessageQueue>> queues = new ConcurrentHashMap<Integer, Queue<MessageQueue>>();

//	@PostConstruct
	public void init() {
		// 处理消息
		methodScheduler.submit(new Runnable() {

			@Override
			public void run() {

				while (true) {
					if (queues.isEmpty()) {
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						continue;
					}

					for (final Queue<MessageQueue> queue : queues.values()) {
						methodScheduler.submit(new Runnable() {
							@Override
							public void run() {
								while (!queue.isEmpty()) {
									final MessageQueue msg = queue.poll();
									final IoSession session = msg
											.getIoSession();

									try {
										push(session, msg.getMsg());
									} catch (Exception e) {
										e.printStackTrace();
										errLogger.error(e.getMessage(), e);
									}
								}
							}
						});
					}
				}
			}
		});

	}

	/** push msg */
	public void push(final IoSession session, final Object message) {

		Request request = (Request) message;
		int code = 0;
		Object result = null;
		try {
			try {
				result = beanInvoker.dispatch(session, request);
			} catch (InvocationTargetException e) {

				Throwable throwable = e.getTargetException();
				if (throwable instanceof Exception) {
					throw (Exception) throwable;
				}
				code = ServerException.system_error;
				errLogger.error(throwable.getMessage(), throwable);
			} catch (IllegalArgumentException e) {
				errLogger.error("{}找不到接口,{}", request.toString(),
						e.getMessage());
				AssertCode.error(ServerException.arg_error);
			}
		} catch (CodeException e) {
			code = e.getCode();

			if (code == ServerException.server_msg_no_return) {
				return;
			}

			// e.printStackTrace();

			// logger.error("服务端异常:" + getStackMsg(e), e);
		} catch (Exception e) {
			code = ServerException.system_error;

			MethodCache methodCache = CmdInit.cmdMethodCahce.get(request
					.getCmd());

			// logger.error("服务端异常:{} -> {} \n {} ", message,
			// methodCache,
			// getStackMsg(e));

			logger.error("服务端异常:" + getStackMsg(e), e);
			e.printStackTrace();
			// errLogger.error(e.getMessage(), e);
		}

		Response response = RequestParseUtil.parser(request, code, result);

		if (Log.respLogger.isDebugEnabled()) {
			Log.respLogger.debug("响应:" + new ResponseVO(response).toString());
		}
		session.write(response);

	}

	@Override
	public void messageReceived(final IoSession session, final Object message)
			throws Exception {

		Integer uid = (Integer) session.getAttribute("uid");

		if (uid == null||uid!=null) {// 直接执行
			methodScheduler.submit(new Runnable() {
				@Override
				public void run() {
					try {
						push(session, message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			Queue<MessageQueue> queue = queues.get(uid);
			if (queue == null) {
				queue = new ConcurrentLinkedQueue<MessageQueue>();
				queues.put(uid, queue);
			}

			MessageQueue msg = new MessageQueue(session, message);
			queue.add(msg);
		}

	}

	private static String getStackMsg(Exception e) {

		StringBuffer sb = new StringBuffer("\n");
		StackTraceElement[] stackArray = e.getStackTrace();
		for (int i = 0; i < stackArray.length; i++) {
			StackTraceElement element = stackArray[i];
			sb.append(element.toString() + "\n");
		}
		return sb.toString();
	}


	private final static String xmls = "<cross-domain-policy><allow-access-from domain='*' to-ports='*'/></cross-domain-policy>\0";

	@Override
	public void sessionOpened(IoSession session) throws Exception {
//		session.write(xmls);
		logger.debug("建立了新的连接{}, 来自：{}", session.getId(),
				session.getRemoteAddress());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		Integer uid = (Integer) session.getAttribute("uid");

		if (uid != null) {
			if (session.containsAttribute("sys.close")) { // 服务端主动关闭,重复登录

			} else {
				sessionAware.remove(uid);
				sessionAware.addDislineUid(uid);
				for (SessionListener listener : listenerInitialization.sessionListeners) {
					listener.onSessionClose(uid);
				}
			}
		}
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {

		if (session.isConnected()) {
			// TODO 暂时不开启，避免影响
			// session.write(heart);// 心跳
			// session.write(0);// 心跳
			Integer uid = (Integer) session.getAttribute("uid");
			if (uid == null) {
				session.close(true);
			}
		} else {
			session.close(true);
		}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {

		if (cause instanceof IOException) {
			// logger.error("IOException" + cause.getMessage(), cause);
		} else if (cause instanceof ProtocolCodecException) {
			logger.error("ProtocolCodecException" + cause.getMessage(), cause);
		} else {
			logger.error("EEException" + cause.getMessage(), cause);
		}

	}

}
