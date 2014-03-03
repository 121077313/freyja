package org.freyja.server.exception;

public class CodeException extends RuntimeException {

	private int code;

	public CodeException() {
		super();
	}

	public CodeException(int code) {
		super(new StringBuffer("").append(code).toString());
		this.code = code;
	}

	public CodeException(int code, String msg) {
		super(new StringBuffer(msg).append(",code = ").append(code).toString());
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
