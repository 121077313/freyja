package org.freyja.server.core;

import java.util.Set;

import org.apache.mina.core.session.IoSession;

public interface SessionAware {

	/** 推送消息 */
	public void write(Integer uid, Object msg);

	/** 获取会话总数 */
	public int getSessionSize();

	/** 获取断线数量 */
	public int getDisLineSize();

	/** 获取离线用户集合 */
	public Set<Integer> getDisLineUids();

	/** 获取当前在线UID集合 */
	public Set<Integer> getOnLineUids();

	/** 添加session */
	public void add(Integer uid, IoSession session);

	public IoSession getIoSession(Integer uid);
	/** 移除session */
	public void remove(Integer uid);

	/** 添加离线uid */
	public void addDislineUid(Integer uid);

	/** 移除离线uid */
	public void removeDislineUid(Integer uid);

	/**
	 * 清理掉一批离线用户
	 * 
	 * @param second
	 *            秒，清理掉下线时间超过xx秒的用户
	 * */
	public void cleanOfflineMap(int second);

	/** 判断在线列表是否存在uid */
	public boolean containsUid(Integer uid);

	/** 判断离线列表是否存在uid */
	public boolean containsOfflineUid(Integer uid);

	public boolean close(Integer uid);

}
