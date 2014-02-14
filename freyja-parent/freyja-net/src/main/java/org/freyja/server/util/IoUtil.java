package org.freyja.server.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.freyja.server.bo.MethodCache;
import org.freyja.server.core.CmdInit;
import org.freyja.server.mina.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public class IoUtil {

	private static final Logger logger = LoggerFactory.getLogger(IoUtil.class);

	/** 使用json格式响应转换成字节数组 */
	public static byte[] toBytesByJson(Response response) {

		if (response.getResult() == null) {
			IoBuffer buf = IoBuffer.allocate(8);
			buf.putInt(response.getCmd());
			buf.putInt(response.getStatus());
			buf.flip();
			byte[] bytes = buf.array();
			buf.clear();

			return bytes;
		}
		byte[] body = JSON.toJSONBytes(response.getResult());

		// 消息别名+状态+消息体
		int capacity = 8 + body.length;
		IoBuffer buf = IoBuffer.allocate(capacity);
		buf.putInt(response.getCmd());
		buf.putInt(response.getStatus());
		buf.put(body);
		buf.flip();
		byte[] bytes = buf.array();
		buf.clear();

		return bytes;
	}

}
