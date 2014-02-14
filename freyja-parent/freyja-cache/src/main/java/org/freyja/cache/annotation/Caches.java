package org.freyja.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Caches {

	CacheSave[] save() default {};

	CacheDelete[] delete() default {};

	CacheListAdd[] add() default {};

	CacheListRemove[] remove() default {};

	CacheListReplace[] replace() default {};
}
