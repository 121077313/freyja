package org.freyja.server.mina.filter.codec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.freyja.server.mina.message.Response;
import org.freyja.server.util.ProbuffIoUtil;

/** PROBUFF解析器 */
public class ResponseProtobufEncoder extends ProtocolEncoderAdapter {

	Map<String, byte[]> buffCache = new ConcurrentHashMap<String, byte[]>();

	@Override
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {

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
				bytes = ProbuffIoUtil.toBytes(command);

				if (key != null) {
					buffCache.put(key, bytes);
				}
			}

		} else {
			bytes = message.toString().getBytes();
		}

		IoBuffer buf = IoBuffer.allocate(bytes.length + 4, false);
		buf.setAutoExpand(false);
		buf.putInt(bytes.length);
		buf.put(bytes);
		buf.flip();
		
		
//System.out.println("字节长度"+bytes.length + 4);


		out.write(buf);
	}

}
