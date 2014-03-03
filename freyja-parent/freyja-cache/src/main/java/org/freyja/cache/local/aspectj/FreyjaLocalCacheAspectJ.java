package org.freyja.cache.local.aspectj;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.freyja.cache.CacheExpressionRootObject;
import org.freyja.cache.FreyjaEvaluationContext;
import org.freyja.cache.local.annotation.FreyjaLocalCacheable;
import org.freyja.cache.operation.FreyjaCacheOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class FreyjaLocalCacheAspectJ {

	static Logger log = LoggerFactory.getLogger(FreyjaLocalCacheAspectJ.class);

	private CacheManager cacheManager;

	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

		MethodSignature m = (MethodSignature) joinPoint.getSignature();
		Method method = m.getMethod();

		FreyjaLocalCacheable entry = method
				.getAnnotation(FreyjaLocalCacheable.class);

		if (entry == null) {
			return joinPoint.proceed();
		}

		Object[] args = joinPoint.getArgs();
		Class<?> clazz = method.getClass();

		Object result = null;

		Object cacheKey = null;

		String keyString = entry.key();
		if (keyString != null && !keyString.equals("")) {
			cacheKey = getCacheKey(keyString, method, args, null,
					joinPoint.getTarget(), joinPoint.getTarget().getClass());
		}

		for (String cacheName : entry.value()) {
			Cache cache = cacheManager.getCache(cacheName);
			if (cache == null) {
				log.error("不存在的缓存空间!:{}", cacheName);
				continue;
			}
			ValueWrapper wrapper = cache.get(cacheKey);

			if (wrapper != null) {
				return wrapper.get();
			}

			result = joinPoint.proceed();
			cache.put(cacheKey, result);

			return result;
		}

		return result;
	}

	private static SpelExpressionParser parser = new SpelExpressionParser();

	private Map<String, Expression> keyCache = new ConcurrentHashMap<String, Expression>();

	private ParameterNameDiscoverer paramNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	private Map<String, Method> targetMethodCache = new ConcurrentHashMap<String, Method>();

	protected Object getCacheKey(String key, Method method, Object[] args,
			Object returnValue, Object target, Class<?> targetClass) {

		CacheExpressionRootObject rootObject = new CacheExpressionRootObject(
				method, args, returnValue, target, targetClass);

		EvaluationContext context = new FreyjaEvaluationContext(rootObject,
				paramNameDiscoverer, method, args, returnValue, targetClass,
				targetMethodCache);
		Expression exp = getExp(key);
		return exp.getValue(context);
	}

	private Expression getExp(String key) {
		Expression exp = keyCache.get(key);
		if (exp == null) {
			exp = parser.parseExpression(key);
			keyCache.put(key, exp);
		}
		return exp;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	private static <T extends Annotation> Collection<T> getAnnotations(
			AnnotatedElement ae, Class<T> annotationType) {
		Collection<T> anns = new ArrayList<T>();

		// look at raw annotation
		T ann = ae.getAnnotation(annotationType);
		if (ann != null) {
			anns.add(ann);
		}

		// scan meta-annotations
		for (Annotation metaAnn : ae.getAnnotations()) {
			ann = metaAnn.annotationType().getAnnotation(annotationType);
			if (ann != null) {
				anns.add(ann);
			}
		}

		return anns;
	}
}
