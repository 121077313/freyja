package org.freyja.server.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

	@Value("${internal_port}")
	public int internalPort;

	@Value("${socket_json_port}")
	public String socketJsonPort;


	@Value("${twg_socket_port}")
	public String TWGSocketSocketPort;
	
	@Value("${socket_websocket_port}")
	public String socketWebSocketPort;

	@Value("${socket_protobuf_port}")
	public String socketProtobufPort;

	/** protobuf 是否需要排序,如果排序,字段会以字段名字的hashcode大小来排序 */
	@Value("${protobuf_order}")
	public boolean protobufOrder;

	@Value("${old_req_json}")
	public boolean oldReqJson;
	
	
}
