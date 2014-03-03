package org.freyja.cache.aspectj;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.beans.BeanMap;
import net.sf.ehcache.Ehcache;

import org.apache.commons.beanutils.PropertyUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.freyja.cache.CacheExpressionRootObject;
import org.freyja.cache.FreyjaCacheAnnotationParser;
import org.freyja.cache.FreyjaEvaluationContext;
import org.freyja.cache.operation.CacheDeleteOperation;
import org.freyja.cache.operation.CacheGetOperation;
import org.freyja.cache.operation.CacheKeysOperation;
import org.freyja.cache.operation.CacheListAddOperation;
import org.freyja.cache.operation.CacheListRemoveOperation;
import org.freyja.cache.operation.CacheListReplaceOperation;
import org.freyja.cache.operation.CacheSaveOperation;
import org.freyja.cache.operation.CacheSetOperation;
import org.freyja.cache.operation.FreyjaCacheOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class FreyjaCacheAspectJ {

	private CacheManager cacheManager;

	private FreyjaCacheAnnotationParser annotationParser = new FreyjaCacheAnnotationParser();

	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

		MethodSignature m = (MethodSignature) joinPoint.getSignature();
		Method method = m.getMethod();

		Collection<CacheOperation> ops = annotationParser
				.parseCacheAnnotations(method);

		if (ops == null || ops.size() == 0) {
			return joinPoint.proceed();
		}

		Object returnValue = null;

		returnValue = joinPoint.proceed();

		Object[] args = joinPoint.getArgs();
		Class<?> clazz = method.getClass();

		Object rid = null;
		if (returnValue != null) {
			BeanMap map = BeanMap.create(returnValue);

			for (CacheOperation opp : ops) {
				if (opp instanceof CacheListReplaceOperation
						|| opp instanceof CacheListRemoveOperation) {

					if ((BeanUtils.getPropertyDescriptor(
							method.getReturnType(), "id") == null)) {
						throw new RuntimeException("aop cache 必须重写getId()方法");
					}
					rid = map.get("id");
					break;
				}
			}
		}

		for (CacheOperation opp : ops) {
			FreyjaCacheOperation op = (FreyjaCacheOperation) opp;

			if (!op.getCondition().equals("")) {
				boolean f;
				if (op.isKeyValueFromReturn()) {

					if (returnValue == null) {
						continue;
					}

					f = condition(op.getCondition(), method, args, returnValue,
							joinPoint.getTarget(), joinPoint.getTarget()
									.getClass());
				} else {
					f = condition(op.getCondition(), method, args, null,
							joinPoint.getTarget(), joinPoint.getTarget()
									.getClass());
				}
				if (!f) {
					continue;
				}
			}

			Object cacheKey = null;

			if (op.getKey() != null && !op.getKey().equals("")) {

				if (op.isKeyValueFromReturn()) {
					if (returnValue == null) {
						continue;
					}
					cacheKey = getCacheKey(op.getKey(), method, args,
							returnValue, joinPoint.getTarget(), joinPoint
									.getTarget().getClass());
				} else {
					cacheKey = getCacheKey(op.getKey(), method, args, null,
							joinPoint.getTarget(), joinPoint.getTarget()
									.getClass());
				}
			}
			for (String cacheName : op.getCacheNames()) {
				Cache cache = cacheManager.getCache(cacheName);

				ValueWrapper wrapper = cache.get(cacheKey);

				if (wrapper == null) {
					if (op instanceof CacheSaveOperation) {
						if (returnValue == null) {
							throw new RuntimeException("this cache not null");
						}
					} else {
						if (op instanceof CacheGetOperation
								|| op instanceof CacheKeysOperation
								|| op instanceof CacheSetOperation) {

						} else {
							continue;
						}

					}
				}

				// synchronized (this) {
				if (op instanceof CacheDeleteOperation) {
					cache.put(cacheKey, null);
				} else if (op instanceof CacheSaveOperation) {
					cache.put(cacheKey, returnValue);
				} else if (op instanceof CacheListAddOperation) {
					List c = (List) wrapper.get();
					c.add(returnValue);
					cache.put(cacheKey, c);
				} else if (op instanceof CacheListRemoveOperation) {
					List c = (List) wrapper.get();
					int i = 0;
					for (Object bean : c) {
						Object id = PropertyUtils.getProperty(bean, "id");
						if (rid.equals(id)) {
							break;
						}
						i++;
					}

					if (i == c.size()) {
						throw new RuntimeException("缓存不一致");
					}

					c.remove(i);
					cache.put(cacheKey, c);

				} else if (op instanceof CacheListReplaceOperation) {
					List c = (List) wrapper.get();
					int i = 0;
					for (Object bean : c) {
						Object id = PropertyUtils.getProperty(bean, "id");

						if (rid.equals(id)) {
							break;
						}
						i++;
					}
					if (i == c.size()) {
						throw new RuntimeException("缓存不一致");
					}

					c.remove(i);
					c.add(returnValue);
					cache.put(cacheKey, c);
				} else if (op instanceof CacheKeysOperation) {
					Object ncache = cache.getNativeCache();
					if (ncache instanceof Ehcache) {
						Ehcache ehcache = (Ehcache) ncache;
						return ehcache.getKeys();
					} else {
						throw new RuntimeException("该缓存不支持缓存keys");
					}
				} else if (op instanceof CacheGetOperation) {
					if (wrapper == null) {
						return null;
					}
					return wrapper.get();
				} else if (op instanceof CacheSetOperation) {
					if (wrapper == null) {
						cache.put(cacheKey, returnValue);
					}
				}

				// }
			}
		}

		return returnValue;
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

	protected boolean condition(String key, Method method, Object[] args,
			Object returnValue, Object target, Class<?> targetClass) {
		CacheExpressionRootObject rootObject = new CacheExpressionRootObject(
				method, args, returnValue, target, targetClass);

		EvaluationContext context = new FreyjaEvaluationContext(rootObject,
				paramNameDiscoverer, method, args, returnValue, targetClass,
				targetMethodCache);
		Expression exp = getExp(key);
		return exp.getValue(context, Boolean.class);
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

}
