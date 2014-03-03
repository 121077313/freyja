package org.freyja.server.exception;

public class ServerException {

	/** 操作成功 */
	public final static int success = 0;

	/** 系统异常 */
	public final static int system_error = -1;

	/** 超时 */
	public final static int timeour = -2;

	/** 请求拒绝 */
	public final static int request_is_refuse = -3;

	/** 没有这个接口 */
	public final static int no_this_interface = -4;

	/** 参数错误 */
	public final static int arg_error = -5;

	/** 数据异常(服务端数据严重错误) */
	public final static int data_exception = -8;

	/** 客户端未验证数据 */
	public final static int client_not_check_data = -9;

	/** 还未登录 */
	public final static int not_login = -10;

	/** 不在同一个服务器 */
	public final static int not_in_same_server = -13;

	
	/** 无法解析消息 */
	public final static int unable_resolve_msg = -15;

	
	
	
	/** 系统已关闭 */
	public final static int server_is_closed = -100;

	/** 服务端不返回消息(内部业务) */
	public final static int server_msg_no_return = -1000;
	
	
	
	
}
