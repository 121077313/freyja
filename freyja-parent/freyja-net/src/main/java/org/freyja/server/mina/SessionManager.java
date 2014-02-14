package org.freyja.server.mina;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;
import org.freyja.log.Log;
import org.freyja.server.bo.ResponseVO;
import org.freyja.server.core.ListenerInit;
import org.freyja.server.core.SessionAware;
import org.freyja.server.listener.SessionListener;
import org.freyja.server.mina.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionManager implements SessionAware {

	private static Logger logger = LoggerFactory
			.getLogger(SessionManager.class);
	@Autowired
	private ListenerInit listenerInitialization;

	/** session集合 */
	private Map<Integer, IoSession> sessionMap = new ConcurrentHashMap<Integer, IoSession>();

	/** 离线用户集合 key:uid value:System.currentTimeMillis */
	private Map<Integer, Long> offlineMap = new ConcurrentHashMap<Integer, Long>();

	/** 单个广播 */
	@Override
	public void write(Integer uid, Object msg) {

		IoSession session = sessionMap.get(uid);

		if (session == null) {
			return;
		}

		if (Log.respLogger.isDebugEnabled()) {
			if (msg instanceof Response) {
				Response response = (Response) msg;
				Log.respLogger.debug("广播:{}",
						new ResponseVO(response).toString());
			}
		}

		if (session == null) {
			return;
		}
		session.write(msg);
	}

	@Override
	public IoSession getIoSession(Integer uid) {
		return sessionMap.get(uid);
	}

	@Override
	public int getSessionSize() {
		return sessionMap.size();
	}

	@Override
	public Set<Integer> getOnLineUids() {
		return sessionMap.keySet();
	}

	@Override
	public void add(Integer uid, IoSession session) {
		sessionMap.put(uid, session);
	}

	@Override
	public void remove(Integer uid) {
		sessionMap.remove(uid);
	}

	@Override
	public int getDisLineSize() {
		return offlineMap.size();
	}

	@Override
	public Set<Integer> getDisLineUids() {
		return offlineMap.keySet();
	}

	@Override
	public void addDislineUid(Integer uid) {
		offlineMap.put(uid, System.currentTimeMillis());
	}

	@Override
	public void removeDislineUid(Integer uid) {
		offlineMap.remove(uid);
	}

	@Override
	public void cleanOfflineMap(int second) {

		long now = System.currentTimeMillis();
		List<Integer> list = new ArrayList<Integer>();
		for (Entry<Integer, Long> entry : offlineMap.entrySet()) {
			long time = entry.getValue();
			int uid = entry.getKey();
			if ((time + 1000l * second) < now) {
				list.add(uid);
			}
		}

		for (Integer uid : list) {
			for (SessionListener listener : listenerInitialization.sessionListeners) {
				listener.onUserRealOffline(uid);
			}
			offlineMap.remove(uid);
		}

	}

	@Override
	public boolean containsUid(Integer uid) {
		return sessionMap.containsKey(uid);
	}

	@Override
	public boolean containsOfflineUid(Integer uid) {
		return offlineMap.containsKey(uid);
	}

	@Override
	public boolean close(Integer uid) {

		IoSession oldSession = getIoSession(uid);

		// 强制关闭旧连接。
		if (null == oldSession) {
			return false;
		}

		logger.debug("强制关闭旧连接 ：{}。", oldSession.getId());
		oldSession.setAttribute("sys.close");// 异步的
		oldSession.close(true);

//		System.out.println("close2:" + uid);
		for (SessionListener listener : listenerInitialization.sessionListeners) {
			listener.onSessionClose(uid);
		}
		return true;
	}

}
