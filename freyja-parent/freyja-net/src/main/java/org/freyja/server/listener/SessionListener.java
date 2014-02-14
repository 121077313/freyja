package org.freyja.server.listener;

/** 监听session、用户在线 状态 */
public interface SessionListener {

	/** session关闭（只是session切换或者重复登录） */
	public void onSessionClose(Integer uid);

	/** 用户确定下线 (这个时候应该序列化用户数据) */
	public void onUserRealOffline(Integer uid);

}
