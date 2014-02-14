package org.freyja.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 
 * 从缓存里面取值
 * 
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheGet {

	/**
	 * Name of the caches in which the update takes place.
	 * <p>
	 * May be used to determine the target cache (or caches), matching the
	 * qualifier value (or the bean name(s)) of (a) specific bean definition.
	 */
	String[] value();

	/**
	 * Spring Expression Language (SpEL) attribute used for conditioning the
	 * cache update.
	 * <p>
	 * Default is "", meaning the method result is always cached.
	 */
	String condition() default "";

	/**
	 * Spring Expression Language (SpEL) attribute for computing the key
	 * dynamically.
	 * <p>
	 * Default is "", meaning all method parameters are considered as a key.
	 */
	String key() default "";

}
