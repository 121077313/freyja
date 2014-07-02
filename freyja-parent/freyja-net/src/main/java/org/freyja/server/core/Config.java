package org.freyja.server.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

	@Value("${internal_port}")
	public int internalPort;

	@Value("${socket_port}")
	public int socketPort;

	/** 请求是否使用protobuf */
	@Value("${request_use_protobuf}")
	public boolean requestUseProtobuf;

	/** 响应是否使用protobuf */
	@Value("${response_use_protobuf}")
	public boolean responseUseProtobuf;
	
	/** protobuf 是否需要排序,如果排序,字段会以字段名字的hashcode大小来排序 */
	@Value("${protobuf_order}")
	public boolean protobufOrder;
	
	
	@Value("${old_req_json}")
	public boolean	oldReqJson;
}
