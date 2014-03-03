package org.freyja.server.util;

import org.apache.mina.core.buffer.IoBuffer;
import org.freyja.server.mina.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.dyuproject.protostuff.runtime.WmsRuntimeSchema;

public class ProbuffIoUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(ProbuffIoUtil.class);

	/** 响应转换成字节数组 */
	public static byte[] toBytes(Response response, boolean protobufOrder) {

		if (response.getResult() == null) {
			IoBuffer buf = IoBuffer.allocate(8);
			buf.putInt(response.getCmd());
			buf.putInt(response.getStatus());
			buf.flip();
			byte[] bytes = buf.array();
			buf.clear();
			return bytes;

		}
		Schema schema;
		if (protobufOrder) {
			schema = WmsRuntimeSchema
					.getSchema(response.getResult().getClass());
		} else {
			schema = RuntimeSchema.getSchema(response.getResult().getClass());
		}

		LinkedBuffer buffer = LinkedBuffer.allocate(2048);
		byte[] body = null;
		try {
			// TODO 采用protostuff 方式序列化
			// body = ProtostuffIOUtil.toByteArray(response.getResult(), schema,
			// buffer);
			body = ProtobufIOUtil.toByteArray(response.getResult(), schema,
					buffer);

		} finally {
			buffer.clear();
		}
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

	/** 请求字节转化成protobuf对象 */
	// public static Req bodyFromBytes(byte[] bytes) {
	// try {
	// Schema<Req> schema = RuntimeSchema.getSchema(Req.class);
	// Req req = new Req();
	// ProtobufIOUtil.mergeFrom(bytes, req, schema);
	// return req;
	// } catch (Exception e) {
	// logger.error(e.getMessage(), e);
	// AssertCode.error(ServerException.unable_resolve_msg);
	// }
	// return null;
	// }

}
