package org.freyja.server.mina.filter.codec;

import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.freyja.server.exception.AssertCode;
import org.freyja.server.exception.ServerException;
import org.freyja.server.mina.message.Request;
import org.freyja.server.util.ProbuffIoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 请求protobuf解析器
 * 
 * @author i see
 * 
 */
public class RequestProtobufDecoder extends CumulativeProtocolDecoder {

	private static final Logger logger = LoggerFactory
			.getLogger(RequestProtobufDecoder.class);

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
				command.setBytes(body);
				command.setJson(false);
				out.write(command);
				// if (logger.isDebugEnabled()) {
				// logger.debug("收到请求:{}" + JSON.toJSONString(command));
				// }

				if (in.remaining() > 0) {// 如果读取内容后还粘了包，就让父类再给俺一次，进行下一次解析
					return true;
				}
			}
		}
		return false;// 处理成功，让父类进行接收下个包
	}

}
