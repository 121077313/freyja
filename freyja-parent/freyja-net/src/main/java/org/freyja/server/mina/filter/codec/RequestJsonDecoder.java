package org.freyja.server.mina.filter.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.freyja.server.exception.AssertCode;
import org.freyja.server.exception.ServerException;
import org.freyja.server.mina.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class RequestJsonDecoder extends CumulativeProtocolDecoder {

	private static final Logger logger = LoggerFactory
			.getLogger(RequestJsonDecoder.class);

//	protected boolean doDecode5(IoSession session, IoBuffer buff,
//			ProtocolDecoderOutput out) throws Exception {
//		int remain = buff.remaining();// byte数
//		if (remain <= 4) {// 至少4字节，1字节=8位，4字节=32位，int占32位
//			return false;
//		}
//		buff.mark();
//		int length = buff.getInt();
//		if (length <= 0) {// 错误的消息协议!!
//			buff.clear();// 清空buff以免影响后面解析
//
//			AssertCode.error(ServerException.unable_resolve_msg);
//			throw new RuntimeException("错误的消息协议!!");
//		}
//
//		if (length + 4 > remain) {
//			buff.reset();
//			return false;
//		}
//
//		int cmd = buff.getInt();
//		byte[] body = new byte[length - 4];
//		buff.get(body, 0, body.length);
//
//		Request command = new Request();
//		command.setCmd(cmd);
//		// 得到了消息主体字节数组
//		// command.bodyFromBytes(body);
//		command.setBytes(body);
//		command.setJson(true);
//		out.write(command);
//		return true;
//	}

	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		// TODO:要注意观察这里,mina让数据包分割后,in.remaining()小于4的话,
		// 后面的getInt() 会抛出异常. 目前还不清楚是否有其他原因造成的.

		if (in.remaining() > 4) {
			in.mark();
			int size = in.getInt();

			if (size > in.remaining()) {
				in.reset();
				return false;
			} else {
				int cmd = in.getInt();
				byte[] body = new byte[size - 4];
				in.get(body);
				Request command = new Request();
				command.setCmd(cmd);
				// 得到了消息主体字节数组
				// command.bodyFromBytes(body);
				command.setJson(true);
				command.setBytes(body);
				out.write(command);
				if (logger.isDebugEnabled()) {
					logger.debug("收到请求:{}", JSON.toJSONString(command));
				}

				if (in.remaining() > 0) {// 如果读取内容后还粘了包，就让父类再给俺一次，进行下一次解析
					return true;
				}
			}
		}
		return false;// 处理成功，让父类进行接收下个包
	}
}
