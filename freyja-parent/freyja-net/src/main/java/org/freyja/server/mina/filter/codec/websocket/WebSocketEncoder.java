/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freyja.server.mina.filter.codec.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.freyja.server.mina.message.Response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

/**
 * Encodes incoming buffers in a manner that makes the receiving client type
 * transparent to the encoders further up in the filter chain. If the receiving
 * client is a native client then the buffer contents are simply passed through.
 * If the receiving client is a websocket, it will encode the buffer contents in
 * to WebSocket DataFrame before passing it along the filter chain.
 * 
 * Note: you must wrap the IoBuffer you want to send around a
 * WebSocketCodecPacket instance.
 * 
 * @author DHRUV CHOPRA
 */
public class WebSocketEncoder extends ProtocolEncoderAdapter {

	Map<String, byte[]> buffCache = new ConcurrentHashMap<String, byte[]>();

	@Override
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {

		boolean isHandshakeResponse = message instanceof WebSocketHandShakeResponse;

		if (isHandshakeResponse) {
			WebSocketHandShakeResponse response = (WebSocketHandShakeResponse) message;
			IoBuffer resultBuffer = WebSocketEncoder
					.buildWSResponseBuffer(response);
			out.write(resultBuffer);
			return;
		}

		String key = null;
		byte[] bytes = null;
		if (message instanceof Response) {
			Response command = (Response) message;
			// cache
			if (command.getResult() == null) {// 只缓存没有返回值的
				key = command.toKey();
				bytes = buffCache.get(key);
			}
			if (bytes == null) {
				bytes = toBytesByJson(command);
				if (key != null) {
					buffCache.put(key, bytes);
				}
			}
		} else {
			bytes = message.toString().getBytes();
		}

		IoBuffer buf = WebSocketEncoder.buildWSDataFrameBuffer(bytes);

		out.write(buf);

	}

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

		JSONArray array = new JSONArray();

		array.add(response.getCmd());

		array.add(response.getStatus());

		array.add(response.getResult());

		return JSON.toJSONBytes(array);
	}

	private static IoBuffer buildWSDataFrameBuffer(byte[] bytes) {

		IoBuffer buffer = IoBuffer.allocate(bytes.length + 2, false);
		buffer.setAutoExpand(true);
		buffer.put((byte) 0x82);
		if (buffer.capacity() <= 125) {
			byte capacity = (byte) (bytes.length);
			buffer.put(capacity);
		} else {
			buffer.put((byte) 126);
			buffer.putShort((short) bytes.length);
		}
		buffer.put(bytes);
		buffer.flip();
		return buffer;

	}

	// Web Socket handshake response go as a plain string.
	private static IoBuffer buildWSResponseBuffer(
			WebSocketHandShakeResponse response) {
		IoBuffer buffer = IoBuffer.allocate(
				response.getResponse().getBytes().length, false);
		buffer.setAutoExpand(true);
		buffer.put(response.getResponse().getBytes());
		buffer.flip();
		return buffer;
	}

}
