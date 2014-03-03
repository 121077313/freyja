package org.freyja.server.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.freyja.server.core.CmdInit;
import org.freyja.server.exception.AssertCode;
import org.freyja.server.exception.ServerException;
import org.freyja.server.mina.message.Request;
import org.freyja.server.mina.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestParseUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(RequestParseUtil.class);
	public static Map<String, Response> responseCache = new ConcurrentHashMap<String, Response>();

	/** 把接口转换成广播 */
	public static Response parser(String interfaceName, Object obj) {
		Request request = new Request();
		// 接口转换成 cmd
		Integer cmd = CmdInit.broadcastCmdStringCache.get(interfaceName);
		if (cmd == null) {
			AssertCode.error(ServerException.no_this_interface);
		}

		request.setCmd(cmd);
		request.setCmdString(interfaceName);
		return parser(request, 0, obj);
	}

	public static Response parser(Request request, int code, Object result) {
		String key = null;
		if (result == null) {
			key = new StringBuffer().append(request.getCmd()).append(".")
					.append(code).toString();
			Response response = responseCache.get(key);
			if (response != null) {
				return response;
			}
		}

		Response response = new Response();
		response.setCmd(request.getCmd());	
		
		response.setStatus(code);
		response.setResult(result);
		
		
		//广播
		response.setCmdString(request.getCmdString());
		if (key != null) {
			responseCache.put(key, response);
		}

		return response;
	}
}
