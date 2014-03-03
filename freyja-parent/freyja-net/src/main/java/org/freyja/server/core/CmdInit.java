package org.freyja.server.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.freyja.server.annotation.Remoting;
import org.freyja.server.annotation.RemotingBroadcast;
import org.freyja.server.bo.MethodCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
/**初始化客户端请求服务端所有命令集合*/
public class CmdInit {

	Logger logger = LoggerFactory.getLogger(CmdInit.class);

	@Autowired
	private ApplicationContext context;

	/** 服务编号-方法缓存映射 */
	public static Map<Integer, MethodCache> cmdMethodCahce = new HashMap<Integer, MethodCache>();

	/** 服务名-服务编号映射 */
	public static Map<String, Integer> broadcastCmdStringCache = new HashMap<String, Integer>();

	/** 用户缓存SystemCache的byte数组 */
	// public static Map<Integer, byte[]> methodBytesCache = new
	// HashMap<Integer, byte[]>();

	@PostConstruct
	public void init() {
		initCmd();
		initBroadcastCmd();
		writeCmdToFile();
	}

	public void initCmd() {
		Map<String, Object> remotingBeans = context
				.getBeansWithAnnotation(Remoting.class);
		for (String serverName : remotingBeans.keySet()) {

			Object obj = context.getBean(serverName);
			Class<?> clazz = obj.getClass();

			// System.out.println(clazz);

			logger.debug("扫描Remoting注解:{}", clazz);

			for (Method method : clazz.getMethods()) {

				// Annotation[]arr= method.getAnnotations();
				// Remoting remoting = method.getAnnotation(Remoting.class);
				Remoting remoting = AnnotationUtils.findAnnotation(method,
						Remoting.class);
				if (remoting == null) {
					continue;
				}
				method.setAccessible(true);
				MethodCache cache = new MethodCache(serverName, method, obj,
						remoting.hasReturn());
				int cmd = remoting.code();
				if (cmd == 0) {
					cmd = cache.getCmd();
				}

				if (cmdMethodCahce.containsKey(cmd)) {// cmd冲突
					throw new RuntimeException("Remoting 重复扫描!!class:"
							+ clazz.getName() + ",methodName:"
							+ cache.getMethodName() + ",cmd:" + cmd);
				}
				cache.setCmd(cmd);
				cmdMethodCahce.put(cmd, cache);
			}
		}

	}

	public void initBroadcastCmd() {
		Map<String, Object> remotingBroadcastBeans = context
				.getBeansWithAnnotation(RemotingBroadcast.class);

		for (String serverName : remotingBroadcastBeans.keySet()) {
			Object obj = context.getBean(serverName);
			Class<?> clazz = obj.getClass();
			for (Field field : clazz.getFields()) {
				field.setAccessible(true);
				String cmdString = "";
				try {
					cmdString = (String) field.get(obj);

				} catch (Exception e1) {
				}

				if (cmdString == null || cmdString.equals("")) {
					cmdString = field.getName();
				} else {
					logger.debug("cmd:{}", cmdString);
				}

				try {
					field.set(obj, cmdString);
				} catch (Exception e) {
					e.printStackTrace();
				}

				int cmd = cmdString.hashCode();
				broadcastCmdStringCache.put(cmdString, cmd);
			}
		}
	}

	public void writeCmdToFile() {
		List<String> cmds = new ArrayList<String>();

		for (MethodCache mechod : cmdMethodCahce.values()) {
			cmds.add(mechod.getCmdString() + ":" + mechod.getCmd());
		}

		for (Entry<String, Integer> entry : broadcastCmdStringCache.entrySet()) {
			cmds.add(entry.getKey() + ":" + entry.getValue());
		}

		try {
			File file = new File("./other/remote.txt");
			FileUtils.writeLines(file, cmds);
		} catch (IOException e) {
			logger.error("生成romote文件失败");
			e.printStackTrace();
		}
	}
}
