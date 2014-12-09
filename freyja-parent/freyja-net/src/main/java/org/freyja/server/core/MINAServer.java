package org.freyja.server.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.freyja.server.mina.ServerHandler;
import org.freyja.server.mina.filter.codec.json.RequestJsonDecoder;
import org.freyja.server.mina.filter.codec.json.ResponseJsonEncoder;
import org.freyja.server.mina.filter.codec.protobuf.RequestProtobufDecoder;
import org.freyja.server.mina.filter.codec.protobuf.ResponseProtobufEncoder;
import org.freyja.server.mina.filter.codec.websocket.WebSocketCodecFactory;
import org.freyja.server.thread.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

public class MINAServer {

	@Autowired
	private ServerHandler handler;

	private static Logger logger = LoggerFactory.getLogger(MINAServer.class);

	@Autowired
	private Config config;

	@PostConstruct
	public void init() throws IOException {
		bindGameSocket();
		bindGameWebSocket();
		bindGameProtobufSocket();
	}

	/**
	 * 绑定游戏服务器
	 * 
	 * @throws IOException
	 */
	private void bindGameSocket() throws IOException {
		NioSocketAcceptor acceptor = new NioSocketAcceptor(16);
		acceptor.setReuseAddress(true);
		acceptor.setBacklog(10000);// 最大连接数量
		acceptor.getSessionConfig().setAll(getSessionConfig());
		DefaultIoFilterChainBuilder filterChain = acceptor.getFilterChain();

		ProtocolEncoderAdapter encoder = null;
		ProtocolDecoderAdapter decoder = null;

		decoder = new RequestJsonDecoder();

		encoder = new ResponseJsonEncoder();
		filterChain.addLast("byteCodecFactory", new ProtocolCodecFilter(
				encoder, decoder));

		// filterChain.addLast("threadPool", new
		// ExecutorFilter(FILTER_EXECUTOR));
		filterChain.addLast(
				"executor",
				new ExecutorFilter(Executors.newFixedThreadPool(20,
						new NamedThreadFactory("mina"))));

		acceptor.setHandler(handler);

		acceptor.bind(new InetSocketAddress(config.socketPort));

		logger.error("bind main socketPort:{}", config.socketPort);
		// probuff

		// NioSocketAcceptor mobileAcceptor = new NioSocketAcceptor();
		// mobileAcceptor.setReuseAddress(true);
		// mobileAcceptor.setBacklog(8000);
		// mobileAcceptor.getSessionConfig().setAll(getSessionConfig());
		// DefaultIoFilterChainBuilder mobileFilterChain = mobileAcceptor
		// .getFilterChain();
		//
		// ResponseEncoder probuffEncoder = new ResponseEncoder();
		//
		// // filterChain.addLast("textCodecFactory", new
		// // ProtocolCodecFilter(text));
		//
		// mobileFilterChain.addLast("byteCodecFactory2", new
		// ProtocolCodecFilter(
		// probuffEncoder, decoder));
		//
		// // mobileFilterChain.addLast("threadPool2", new ExecutorFilter(
		// // FILTER_EXECUTOR));
		//
		// filterChain.addLast("executor2",
		// new ExecutorFilter(Executors.newCachedThreadPool()));
		//
		// mobileAcceptor.setHandler(handler);
		//
		// mobileAcceptor.bind(new InetSocketAddress(socketProbuffPort));
		// logger.error("bind main socketProbuffPort:" + socketProbuffPort);
		//

	}

	void bindGameProtobufSocket() throws IOException {
		if (config.socketProtobufPort == null) {
			return;
		}

		NioSocketAcceptor acceptor = new NioSocketAcceptor(16);
		acceptor.setReuseAddress(true);
		acceptor.setBacklog(10000);// 最大连接数量
		acceptor.getSessionConfig().setAll(getSessionConfig());
		DefaultIoFilterChainBuilder filterChain = acceptor.getFilterChain();

		filterChain.addLast("byteCodecFactory", new ProtocolCodecFilter(
				new ResponseProtobufEncoder(config.protobufOrder),
				new RequestProtobufDecoder()));

		// filterChain.addLast("threadPool", new
		// ExecutorFilter(FILTER_EXECUTOR));
		filterChain.addLast(
				"executor",
				new ExecutorFilter(Executors.newFixedThreadPool(20,
						new NamedThreadFactory("mina"))));

		acceptor.setHandler(handler);

		acceptor.bind(new InetSocketAddress(config.socketProtobufPort));

		logger.error("bind main protobufSocketPort:{}",
				config.socketProtobufPort);
		// probuff

	}

	void bindGameWebSocket() throws IOException {

		if (config.socketWebSocketPort == null) {
			return;
		}
		NioSocketAcceptor acceptor = new NioSocketAcceptor(16);
		acceptor.setReuseAddress(true);
		acceptor.setBacklog(10000);// 最大连接数量
		acceptor.getSessionConfig().setAll(getSessionConfig());
		DefaultIoFilterChainBuilder filterChain = acceptor.getFilterChain();

		filterChain.addLast("byteCodecFactory", new ProtocolCodecFilter(
				new WebSocketCodecFactory()));

		// filterChain.addLast("threadPool", new
		// ExecutorFilter(FILTER_EXECUTOR));
		filterChain.addLast(
				"executor",
				new ExecutorFilter(Executors.newFixedThreadPool(20,
						new NamedThreadFactory("mina"))));

		acceptor.setHandler(handler);

		acceptor.bind(new InetSocketAddress(config.socketWebSocketPort));

		logger.error("bind main websocketPort:{}", config.socketWebSocketPort);
		// probuff

	}

	public SocketSessionConfig getSessionConfig() {
		SocketSessionConfig sessionConfig = new DefaultSocketSessionConfig();
		sessionConfig.setSoLinger(0);
		sessionConfig.setKeepAlive(true);

		sessionConfig.setReuseAddress(true);
		sessionConfig.setTcpNoDelay(true);
		sessionConfig.setBothIdleTime(90);
		sessionConfig.setReadBufferSize(4096);
		sessionConfig.setSendBufferSize(4096);
		sessionConfig.setWriteTimeout(30);
		sessionConfig.setReceiveBufferSize(4096);
		return sessionConfig;
	}
}
