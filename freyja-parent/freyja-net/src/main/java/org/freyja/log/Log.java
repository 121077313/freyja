package org.freyja.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
	public static final Logger logger = LoggerFactory.getLogger(Log.class);
	public static final Logger reqLogger = LoggerFactory.getLogger("REQUEST");
	
	public static Logger respLogger = LoggerFactory.getLogger("RESPONSE");
}
