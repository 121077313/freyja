package org.freyja.server.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.freyja.server.listener.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
/** 监听器初始化容器 */
public class ListenerInit {

	private static final Logger logger = LoggerFactory.getLogger(ListenerInit.class);
	
	public  List<SessionListener> sessionListeners = new ArrayList<SessionListener>();

	@Autowired
	private ApplicationContext context;

	@PostConstruct
	public void init() {
		logger.info("初始化SessionListener");
		Map<String, SessionListener> listeners = context
				.getBeansOfType(SessionListener.class);
		sessionListeners.addAll(listeners.values());
	}

}
