package org.freyja.server.net.protobuf;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.freyja.server.annotation.GameVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import protobuf.JavaToProto;
import protobuf.JavaToProto.Builder;

/**
 * 生成proto文件
 * 
 * @author i see
 * 
 */
@Component
public class ProtoGenerator {

	@Autowired
	private ApplicationContext context;

	@PostConstruct
	public void init() {
		createOneFile();
//		writeCmdToFile();
	}

	private void createOneFile() {

		// 将proto 合成一个文件
		Builder builder = JavaToProto.createBuilder();

		Map<String, Object> vos = context.getBeansWithAnnotation(GameVO.class);

		for (String serverName : vos.keySet()) {
			Object obj = context.getBean(serverName);
			Class<?> clazz = obj.getClass();

			builder.registery(clazz);
		}

		builder.flush();

	}

	public void writeCmdToFile() {
		Map<String, Object> vos = context.getBeansWithAnnotation(GameVO.class);

		for (String serverName : vos.keySet()) {
			Object obj = context.getBean(serverName);
			Class<?> clazz = obj.getClass();
			JavaToProto jpt = new JavaToProto(clazz);
			try {
				String protoFile = jpt.toString();

				File file = new File("./other/proto/" + clazz.getSimpleName()
						+ ".proto");
				FileUtils.writeStringToFile(file, protoFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
