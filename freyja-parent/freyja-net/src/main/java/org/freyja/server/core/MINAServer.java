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
import org.freyja.server.mina.filter.codec.RequestJsonDecoder;
import org.freyja.server.mina.filter.codec.RequestProtobufDecoder;
import org.freyja.server.mina.filter.codec.ResponseProtobufEncoder;
import org.freyja.server.mina.filter.codec.ResponseJsonEncoder;
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

		// filterChain.addLast("connectionThrottleFilter",
		// new ConnectionThrottleFilter(1000l, 10000));// 连续请求必须在1000ms以上
		//
		// TODO: 关闭请求时间过滤器
		// filterChain.addLast("attackFilter", new CmdAttackFilter(500l,
		// 10000));// 连续请求必须在1000ms以上

		// TextLineCodecFactory text = new TextLineCodecFactory(
		// Charset.forName("UTF-8"), LineDelimiter.UNIX,
		// LineDelimiter.UNIX);
		//
		// text.setDecoderMaxLineLength(Integer.MAX_VALUE);
		// text.setEncoderMaxLineLength(Integer.MAX_VALUE);

		// ProtocolEncoderAdapter encoder = new ResponseEncoder();

		ProtocolEncoderAdapter encoder = null;
		ProtocolDecoderAdapter decoder = null;

		if (config.requestUseProtobuf) {
			decoder = new RequestProtobufDecoder();
		} else {// json
			decoder = new RequestJsonDecoder();
		}

		if (config.responseUseProtobuf) {
			encoder = new ResponseProtobufEncoder(config.protobufOrder);
		} else {// json
			encoder = new ResponseJsonEncoder();
		}
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
