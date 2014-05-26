package org.freyja.server.exception;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.freyja.log.Log;

public class AssertCode {

	static Map<Integer, RuntimeException> map = new ConcurrentHashMap<Integer, RuntimeException>();

	/**
	 * 错误码
	 * 
	 * @throws CodeException
	 */
	public static void error(int error) {

		
//		if(1==1) {
//			throw new CodeException(error);
//		}
		
		
		RuntimeException e = map.get(error);
		// 缓存exception 会导致异常堆栈显示旧的

		if (Log.logger.isDebugEnabled()) {

			throw new CodeException(error);
		}
		// 通过这种方式来判断是否缓存异常
		if (e == null) {
			e = new CodeException(error);
			map.put(error, e);
		}
		throw e;
		// throw new CodeException(error);
	}

	public static void error(int error, String msg) {

		error(error);
		// if (msg == null) {
		// error(error);
		// return;
		// }
		// throw new CodeException(error, msg);
	}

}
