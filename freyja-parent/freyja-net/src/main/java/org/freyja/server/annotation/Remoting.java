package org.freyja.server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
@Documented
/**是否提供远程服务（允许客户端调用）*/
public @interface Remoting {

	/** 指令代码号 */
	int code() default 0;

	/** 是否有返回值(下行) */
	boolean hasReturn() default true;
}
