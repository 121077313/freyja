package org.freyja.server.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.freyja.server.mina.InternalHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class InternalServer {

	@Autowired
	private InternalHandler handler;

	private static Logger logger = LoggerFactory
			.getLogger(InternalServer.class);

	@Autowired
	private Config config;

	@PostConstruct
	public void init() throws IOException {
		NioSocketAcceptor acceptor = new NioSocketAcceptor(16);
		acceptor.setReuseAddress(true);
		DefaultIoFilterChainBuilder filterChain = acceptor.getFilterChain();
		filterChain.addLast("codec", new ProtocolCodecFilter(
				new TextLineCodecFactory(Charset.forName("UTF-8"),
						LineDelimiter.UNIX, LineDelimiter.UNIX

				)));
		acceptor.setHandler(handler);

		acceptor.bind(new InetSocketAddress(config.internalPort));

		logger.error("bind mina internalPort:{}", config.internalPort);
	}
}
