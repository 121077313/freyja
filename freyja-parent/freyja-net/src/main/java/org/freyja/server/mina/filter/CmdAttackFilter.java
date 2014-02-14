package org.freyja.server.mina.filter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;

/**
 * A {@link IoFilter} which blocks connections from connecting
 * at a rate faster than the specified interval.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
/** 防攻击,消息间隔 */
public class CmdAttackFilter extends IoFilterAdapter {
	private static final long DEFAULT_TIME = 1000;

	private long allowedInterval;

	private final Map<String, Long> clients;

	private final static Logger LOGGER = LoggerFactory
			.getLogger(CmdAttackFilter.class);

	/**
	 * Default constructor. Sets the wait time to 1 second
	 */
	public CmdAttackFilter() {
		this(DEFAULT_TIME, 10000);
	}

	/**
	 * Constructor that takes in a specified wait time.
	 * 
	 * @param allowedInterval
	 *            The number of milliseconds a client is allowed to wait before
	 *            making another successful connection
	 * @param maxSize
	 *            最大缓存数量
	 * 
	 * 
	 */
	public CmdAttackFilter(long allowedInterval, int maxSize) {
		this.allowedInterval = allowedInterval;

		clients = new ConcurrentLinkedHashMap.Builder<String, Long>()
				.maximumWeightedCapacity(maxSize).weigher(Weighers.singleton())
				.build();

	}

	/**
	 * Sets the interval between connections from a client. This value is
	 * measured in milliseconds.
	 * 
	 * @param allowedInterval
	 *            The number of milliseconds a client is allowed to wait before
	 *            making another successful connection
	 */
	public void setAllowedInterval(long allowedInterval) {
		this.allowedInterval = allowedInterval;
	}

	/**
	 * Method responsible for deciding if a connection is OK to continue
	 * 
	 * @param session
	 *            The new session that will be verified
	 * @return True if the session meets the criteria, otherwise false
	 */
	protected boolean isConnectionOk(IoSession session) {
		SocketAddress remoteAddress = session.getRemoteAddress();
		if (remoteAddress instanceof InetSocketAddress) {
			InetSocketAddress addr = (InetSocketAddress) remoteAddress;
			long now = System.currentTimeMillis();

			if (clients.containsKey(addr.getAddress().getHostAddress())) {

				LOGGER.debug("This is not a new client");
				Long lastConnTime = clients.get(addr.getAddress()
						.getHostAddress());

				clients.put(addr.getAddress().getHostAddress(), now);

				// if the interval between now and the last connection is
				// less than the allowed interval, return false
				if (now - lastConnTime < allowedInterval) {
					LOGGER.warn("Session connection interval too short");
					return false;
				}

				return true;
			}

			clients.put(addr.getAddress().getHostAddress(), now);
			return true;
		}

		return false;
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		if (!isConnectionOk(session)) {
			LOGGER.warn("message coming in too fast; block.");
			
		}else{
			super.messageReceived(nextFilter, session, message);
		}

	}

}
