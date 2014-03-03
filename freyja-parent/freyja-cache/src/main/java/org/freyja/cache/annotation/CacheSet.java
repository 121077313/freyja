package org.freyja.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 该标签对应Spring的CachePut ，CachePut只能根据参数来生成key， CacheSet可以通过返回值来生成key
 * 
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheSet {

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

	/** key和condition的取值是否来自于返回值 */
	boolean keyValueFromReturn() default true;
}
