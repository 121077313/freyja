package org.freyja.server.core;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.freyja.log.Log;
import org.freyja.server.bo.BeanInvoker;
import org.freyja.server.bo.ExpressionBO;
import org.freyja.server.bo.MethodCache;
import org.freyja.server.bo.RequestInvoker;
import org.freyja.server.bo.RequestVO;
import org.freyja.server.exception.AssertCode;
import org.freyja.server.exception.ServerException;
import org.freyja.server.mina.message.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.dyuproject.protostuff.runtime.WmsRuntimeSchema;

@Component
public class Invoker {

	@Autowired
	private ApplicationContext context;

	Map<Class<?>, BeanInvoker<?>> invokerMap = new HashMap<Class<?>, BeanInvoker<?>>();

	/** 参数命名不能重复 */
	Map<String, BeanInvoker<?>> invokerNameMap = new HashMap<String, BeanInvoker<?>>();

	private static final Logger logger = LoggerFactory.getLogger(Invoker.class);

	/**
	 * 扫描并缓存 BeanInvoker
	 */
	@PostConstruct
	public void init() {

		Map<String, BeanInvoker> invokers = context
				.getBeansOfType(BeanInvoker.class);

		for (BeanInvoker<?> invoker : invokers.values()) {

			try {
				Type t = invoker.getClass().getGenericInterfaces()[0];
				Class<?> entityClass = (Class<?>) ((ParameterizedType) t)
						.getActualTypeArguments()[0];
				invokerMap.put(entityClass, invoker);

				invokerNameMap.put(invoker.getName(), invoker);
			} catch (Exception e) {
				logger.error("BeanInvoker实现类写法不合法，请按照标准编写。例如：public class UserInvoker implements BeanInvoker<User>");
				e.printStackTrace();
			}
		}

	}

	/** 分发请求 */
	public Object dispatch(IoSession session, Request request)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		MethodCache methodCache = CmdInit.cmdMethodCahce.get(request.getCmd());

		if (methodCache == null || methodCache.getMethod() == null) {
			AssertCode.error(ServerException.no_this_interface);// 没有这个接口
		}

		request.setCmdString(methodCache.getCmdString());

		if (!config.oldReqJson) {// req protobuf,
			return dispatch(methodCache, session, request);
		}
		// param is JsonArray

		request.bodyFromBytes(request.getBytes());

		if (Log.reqLogger.isDebugEnabled()) {
			Log.reqLogger.debug("请求:{}", new RequestVO(request).toString());
		}

		Method method = methodCache.getMethod();

		int len = method.getParameterTypes().length;
		int reqlen = request.getParameters().size();

		Integer uid = (Integer) session.getAttribute("uid");

		Object[] parameters = new Object[len];
		for (int i = 0; i < len; i++) {
			Class<?> clazz = method.getParameterTypes()[i];

			if (i < reqlen) {
				Object pobj = request.getParameters().get(i);
				parameters[i] = pobj;
			} else {
				BeanInvoker<?> invoker = invokerMap.get(clazz);
				if (invoker == null) {
					logger.error("接口：{} 必填参数：[{},{}] 不能为空,没有该Invoker",
							request.getCmd(), i, clazz.getName());

					AssertCode.error(ServerException.arg_error);// 参数错误
				}

				parameters[i] = invoker.invoke(uid, session);
				if (parameters[i] == null) {
					AssertCode.error(ServerException.arg_error);
				}
			}
		}

		// 调用方法
		Object service = methodCache.getService();
		Object result = method.invoke(service, parameters);

		if (!methodCache.isHasReturn()) {

			AssertCode.error(ServerException.server_msg_no_return, "服务端不返回消息");

		}

		return result;
	}

	private ParameterNameDiscoverer paramNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	private Object dispatch(MethodCache methodCache, IoSession session,
			Request request) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		Object req = null;
		if (request.getBytes().length > 0) {

			req = requestInvoker.invoke();
			if (request.isJson()) {
				try {
					req = JSON.parseObject(request.getBytes(), req.getClass());
				} catch (Exception e) {

					logger.error("请求字符不是Json格式." + e.getMessage(), e);

					String str = new String(request.getBytes());
					logger.error("错误的json格式:{}", str);
				}
			} else {
				Schema schema = null;
				if (config.protobufOrder) {
					schema = WmsRuntimeSchema.getSchema(req.getClass());
				} else {
					schema = RuntimeSchema.getSchema(req.getClass());
				}

				try {
					ProtobufIOUtil.mergeFrom(request.getBytes(), req, schema);
				} catch (Exception e) {
					logger.error("请求字符不是probuf编码字符," + e.getMessage(), e);
				}

			}

		}

		if (Log.reqLogger.isDebugEnabled()) {
			request.setParameter(req);
			Log.reqLogger.debug("请求:{}", new RequestVO(request).toString());
		}

		StandardEvaluationContext context = new StandardEvaluationContext(req);
		ExpressionBO bo = expressionCache.get(methodCache.getCmd());
		Expression expression = bo.getExpression();

		Integer uid = (Integer) session.getAttribute("uid");

		List<String> names = bo.getList();

		// for (BeanInvoker invoker : invokerMap.values()) {
		// String name = invoker.getName();
		// if (!names.contains("#" + name)) {
		// continue;
		// }
		// Object obj = invoker.invoke(uid, session);
		// context.setVariable(name, obj);
		// }

		for (String name : names) {
			if (!name.startsWith("#")) {
				continue;
			}
			String invokerName = name.substring(1, name.length());
			BeanInvoker invoker = invokerNameMap.get(invokerName);
			if (invoker == null) {
				continue;
			}

			Object obj = invoker.invoke(uid, session);
			context.setVariable(invokerName, obj);
		}

		Object[] args = expression.getValue(context, Object[].class);

		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				AssertCode.error(ServerException.arg_error, "参数错误,部分参数为空");
			}
		}
		Method method = methodCache.getMethod();
		Object result = method.invoke(methodCache.getService(), args);

		if (!methodCache.isHasReturn()) {
			AssertCode.error(ServerException.server_msg_no_return, "服务端不返回消息");
		}
		return result;
	}

	Map<Integer, ExpressionBO> expressionCache = new ConcurrentHashMap<Integer, ExpressionBO>();
	RequestInvoker requestInvoker = null;

	@Autowired
	private Config config;

	@PostConstruct
	public void scan() {
		// if (!config.requestUseProtobuf) {
		// return;
		// }

		Map<String, RequestInvoker> invokers = context
				.getBeansOfType(RequestInvoker.class);
		List<RequestInvoker> reqInvokers = new ArrayList<RequestInvoker>(
				invokers.values());

		if (reqInvokers.size() != 1) {
			logger.error("RequestInvoker 缺少或者过多");
			return;
		}

		requestInvoker = reqInvokers.get(0);
		Object obj = requestInvoker.invoke();
		for (MethodCache methodCache : CmdInit.cmdMethodCahce.values()) {

			Method method = methodCache.getMethod();
			String[] names = paramNameDiscoverer.getParameterNames(method);

			// 判断参数是否有重复

			if (names == null) {
				names = new String[] {};
			}

			List<String> list = Arrays.asList(names);

			Set<String> set = new HashSet<String>(list);

			if (set.size() != list.size()) {
				throw new RuntimeException("方法参数名不允许重复!:"
						+ methodCache.getService().getClass().getName() + "."
						+ method.getName());
			}

			for (int i = 0; i < names.length; i++) {
				String name = names[i];
				for (BeanInvoker invoker : invokerMap.values()) {
					if (!name.equals(invoker.getName())) {
						continue;
					}

					names[i] = "#" + name;
					break;
				}
			}

			ExpressionParser parser = new SpelExpressionParser();

			String expressionString = new StringBuffer("{")
					.append(StringUtils.join(names, ",")).append("}")
					.toString();
			Expression expression = parser.parseExpression(expressionString);

			StandardEvaluationContext context = new StandardEvaluationContext(
					obj);

			try {
				Object[] arr = expression.getValue(context, Object[].class);

				ExpressionBO bo = new ExpressionBO();
				bo.setExpression(expression);
				bo.setNames(names);
				bo.setList(list);
				expressionCache.put(methodCache.getCmd(), bo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
