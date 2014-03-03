package protobuf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.freyja.server.annotation.Proto;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.dyuproject.protostuff.runtime.WmsRuntimeSchema;

public class JavaToProto {

	/** 是否对probuf字段进行排序 */
	private boolean order;

	private static String NAME = "pojo To Proto file";
	private static String VERSION = "v0.2";

	private static String OPEN_BLOCK = "{";
	private static String CLOSE_BLOCK = "}";
	private static String MESSAGE = "message";
	private static String ENUM = "enum";
	private static String NEWLINE = "\n";
	private static String TAB = "\t";
	private static String COMMENT = "//";
	private static String SPACE = " ";
	private static String PATH_SEPERATOR = ".";
	private static String OPTIONAL = "optional";
	private static String REQUIRED = "required";
	private static String REPEATED = "repeated";
	private static String LINE_END = ";";

	// isee
	private static String DEFAULT = "[default = -1]";

	private StringBuilder builder;
	private Stack<Class<?>> classStack = new Stack<Class<?>>();
	private Map<Class<?>, String> typeMap = getPrimitivesMap();
	private int tabDepth = 0;

	String defaultOption = OPTIONAL;

	/**
	 * Entry Point for the CLI Interface to this Program.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length == 0) {
			System.out
					.println("Usage: \n\tjava -jar JavaToProto.jar JavaToProto <class name> [<output file name>]\n");
		}

		Class<?> clazz;

		try {
			clazz = Class.forName(args[0]);
		} catch (Exception e) {
			System.out
					.println("Could not load class. Make Sure it is in the classpath!!");
			e.printStackTrace();
			return;
		}

		JavaToProto jtp = new JavaToProto(clazz, false);

		String protoFile = jtp.toString();

		if (args.length == 2) {
			// Write to File

			try {
				File f = new File(args[1]);
				FileWriter fw = new FileWriter(f);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(protoFile);
				out.flush();
				out.close();
			} catch (Exception e) {
				System.out
						.println("Got Exception while Writing to File - See Console for File Contents");
				System.out.println(protoFile);
				e.printStackTrace();
			}

		} else {
			// Write to Console
			System.out.println(protoFile);
		}

	}

	/**
	 * Creates a new Instance of JavaToProto to process the given class
	 * 
	 * @param classToProcess
	 *            - The Class to be Processed - MUST NOT BE NULL!
	 */
	public JavaToProto(Class<?> classToProcess, boolean order) {
		if (classToProcess == null) {
			throw new RuntimeException(
					"You gave me a null class to process. This cannot be done, please pass in an instance of Class");
		}
		this.order = order;
		classStack.push(classToProcess);
	}

	// region Helper Functions

	public static Builder createBuilder() {
		Builder builder = new Builder();
		return builder;
	}

	public static SchemaBuilder createSchemaBuilder() {
		SchemaBuilder builder = new SchemaBuilder();
		return builder;
	}

	public static class Builder {
		private HashMap<Class<?>, JavaToProto> allProto = new LinkedHashMap<Class<?>, JavaToProto>();

		public void registery(Class<?> classToProcess) {

			allProto.put(classToProcess, new JavaToProto(classToProcess, false));
		}

		public void flush() {

			StringBuilder sb = new StringBuilder();
			sb.append("package com.protobuf;\n");
			for (JavaToProto javatoProto : allProto.values()) {
				try {

					sb.append(javatoProto.toString());
				} catch (RuntimeException e) {
					System.out.println("解析VO:"
							+ javatoProto.classStack.get(0).getName());
					throw e;
				}
			}

			try {
				String protoFile = sb.toString();

				File file = new File("./other/proto/Message" + ".proto");
				FileUtils.writeStringToFile(file, protoFile);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println(" 生成proto文件失败，不支持部分类型：\n"
						+ e.fillInStackTrace());
			}

		}

		public void close() {
			allProto.clear();
		}
	}

	public static class SchemaBuilder {

		private HashMap<Class<?>, JavaToProto> allProto = new LinkedHashMap<Class<?>, JavaToProto>();

		public void registery(Class<?> classToProcess) {
			allProto.put(classToProcess, new JavaToProto(classToProcess, false));
		}

		public void flush() {

			StringBuilder sb = new StringBuilder();
			sb.append("package com.protobuf;\n");
			for (JavaToProto javatoProto : allProto.values()) {
				sb.append(javatoProto.parseFromSchema());
			}

			try {
				String protoFile = sb.toString();

				File file = new File("./other/proto/Message" + ".proto");
				FileUtils.writeStringToFile(file, protoFile);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println(" 生成proto文件失败，不支持部分类型：\n"
						+ e.fillInStackTrace());
			}

		}

		public void close() {
			allProto.clear();
		}
	}

	public String parseFromSchema() {

		if (currentClass().isInterface() || currentClass().isEnum()
				|| Modifier.isAbstract(currentClass().getModifiers())) {

			Class<?> clazz = currentClass();
			// System.out.println( currentClass().getName() );

			throw new RuntimeException(clazz.getName()
					+ "A Message cannot be an Interface, Abstract OR an Enum");
			// test
		}
		builder = new StringBuilder();
		String messageName = currentClass().getSimpleName();

		typeMap.put(currentClass(), getPath());

		builder.append(getTabs()).append(MESSAGE).append(SPACE)
				.append(messageName).append(OPEN_BLOCK).append(NEWLINE);

		tabDepth++;

		parseFromSchemaFields();

		tabDepth--;

		builder.append(getTabs()).append(CLOSE_BLOCK).append(NEWLINE);

		return messageName;

	}

	void orderParseFromSchemaFields() {

		WmsRuntimeSchema schema = (WmsRuntimeSchema) WmsRuntimeSchema
				.getSchema(currentClass());

		int fieldCount = schema.getFieldCount();
		for (int i = 1; i < fieldCount; i++) {

			String name = schema.getFieldName(i);

			Field f = null;
			try {
				f = currentClass().getField(name);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (null == f)
				continue;
			int mod = f.getModifiers();
			if (!Modifier.isPrivate(mod)) {// 添加，只把私有属性传输
				// Skip not private field
				continue;
			}

			i++;

			if (Modifier.isAbstract(mod) || Modifier.isTransient(mod)) {
				// Skip this field
				continue;
			}

			Class<?> fieldType = f.getType();

			// Primitives or Types we have come across before
			if (typeMap.containsKey(fieldType)) {
				processField(OPTIONAL, typeMap.get(fieldType), f.getName(), i);
				continue;
			}

			if (fieldType.isEnum()) {
				processEnum(fieldType);
				processField(REQUIRED, typeMap.get(fieldType), f.getName(), i);
				continue;
			}

			if (Map.class.isAssignableFrom(fieldType)) {

				throw new IllegalArgumentException("protobuf不支持MAP");

			}

			if (Date.class.isAssignableFrom(fieldType)) {

				throw new IllegalArgumentException("protobuf不支持Date");

			}

			if (fieldType.isArray()) {
				Class<?> innerType = fieldType.getComponentType();

				if (!typeMap.containsKey(innerType)) {
					// TODO 不提倡在proto中使用消息嵌套
					// buildNestedType(innerType);
				}
				String typeName = getTypeName(innerType);
				processField(REPEATED, typeName, f.getName(), i);
				continue;
			}

			if (Collection.class.isAssignableFrom(fieldType)) {
				Class<?> innerType = null;

				Type t = f.getGenericType();

				if (t instanceof ParameterizedType) {
					ParameterizedType tt = (ParameterizedType) t;
					innerType = (Class<?>) tt.getActualTypeArguments()[0];
				}

				if (!typeMap.containsKey(innerType)) {
					// buildNestedType(innerType);
				}
				String typeName = getTypeName(innerType);
				processField(REPEATED, typeName, f.getName(), i);
				continue;
			}

			String typeName = getTypeName(fieldType);
			processField(OPTIONAL, typeName, f.getName(), i);
		}

	}

	private void parseFromSchemaFields() {
		if (order) {
			orderParseFromSchemaFields();
			return;
		}
		RuntimeSchema<Object> schema = (RuntimeSchema<Object>) RuntimeSchema
				.getSchema(currentClass());

		int fieldCount = schema.getFieldCount();
		for (int i = 1; i < fieldCount; i++) {

			String name = schema.getFieldName(i);

			Field f = null;
			try {
				f = currentClass().getField(name);
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (null == f)
				continue;
			int mod = f.getModifiers();
			if (!Modifier.isPrivate(mod)) {// 添加，只把私有属性传输
				// Skip not private field
				continue;
			}

			i++;

			if (Modifier.isAbstract(mod) || Modifier.isTransient(mod)) {
				// Skip this field
				continue;
			}

			Class<?> fieldType = f.getType();

			// Primitives or Types we have come across before
			if (typeMap.containsKey(fieldType)) {
				processField(OPTIONAL, typeMap.get(fieldType), f.getName(), i);
				continue;
			}

			if (fieldType.isEnum()) {
				processEnum(fieldType);
				processField(REQUIRED, typeMap.get(fieldType), f.getName(), i);
				continue;
			}

			if (Map.class.isAssignableFrom(fieldType)) {

				throw new IllegalArgumentException("protobuf不支持MAP");

			}

			if (Date.class.isAssignableFrom(fieldType)) {

				throw new IllegalArgumentException("protobuf不支持Date");

			}

			if (fieldType.isArray()) {
				Class<?> innerType = fieldType.getComponentType();

				if (!typeMap.containsKey(innerType)) {
					// TODO 不提倡在proto中使用消息嵌套
					// buildNestedType(innerType);
				}
				String typeName = getTypeName(innerType);
				processField(REPEATED, typeName, f.getName(), i);
				continue;
			}

			if (Collection.class.isAssignableFrom(fieldType)) {
				Class<?> innerType = null;

				Type t = f.getGenericType();

				if (t instanceof ParameterizedType) {
					ParameterizedType tt = (ParameterizedType) t;
					innerType = (Class<?>) tt.getActualTypeArguments()[0];
				}

				if (!typeMap.containsKey(innerType)) {
					// buildNestedType(innerType);
				}
				String typeName = getTypeName(innerType);
				processField(REPEATED, typeName, f.getName(), i);
				continue;
			}

			String typeName = getTypeName(fieldType);
			processField(OPTIONAL, typeName, f.getName(), i);
		}

	}

	public String getTabs() {
		String res = "";

		for (int i = 0; i < tabDepth; i++) {
			res = res + TAB;
		}

		return res;
	}

	public String getPath() {
		String path = "";

		Stack<Class<?>> tStack = new Stack<Class<?>>();

		while (!classStack.isEmpty()) {
			Class<?> t = classStack.pop();
			if (path.length() == 0) {
				path = t.getSimpleName();
			} else {
				path = t.getSimpleName() + PATH_SEPERATOR + path;
			}
			tStack.push(t);
		}

		while (!tStack.isEmpty()) {
			classStack.push(tStack.pop());
		}

		return path;
	}

	public Class<?> currentClass() {
		return classStack.peek();
	}

	public Map<Class<?>, String> getPrimitivesMap() {
		Map<Class<?>, String> results = new HashMap<Class<?>, String>();

		results.put(double.class, "double");
		results.put(float.class, "float");
		results.put(int.class, "int32");
		results.put(long.class, "sint64");
		results.put(boolean.class, "bool");
		results.put(Double.class, "double");
		results.put(Float.class, "float");
		results.put(Integer.class, "int32");
		results.put(Long.class, "sint64");
		results.put(Boolean.class, "bool");
		results.put(String.class, "string");
		return results;
	}

	public void processField(String repeated, String type, String name,
			int index) {

		if (type.equals("int32") && !repeated.equals(REPEATED)) {
			builder.append(getTabs()).append(repeated).append(SPACE)
					.append(type).append(SPACE).append(name).append(SPACE)
					.append("=").append(SPACE).append(index).append(SPACE)
					.append(DEFAULT).append(LINE_END).append(NEWLINE);
		} else {
			builder.append(getTabs()).append(repeated).append(SPACE)
					.append(type).append(SPACE).append(name).append(SPACE)
					.append("=").append(SPACE).append(index).append(LINE_END)
					.append(NEWLINE);
		}

	}

	// end region

	private void generateProtoFile() {
		builder = new StringBuilder();

		// File Header
		/*
		 * builder.append(COMMENT).append("Generated by ").append(NAME)
		 * .append(SPACE).append(VERSION).append(" @ ").append(new Date())
		 * .append(NEWLINE).append(NEWLINE);
		 */

		buildMessage();

	}

	private String buildMessage() {

		if (currentClass().isInterface() || currentClass().isEnum()
				|| Modifier.isAbstract(currentClass().getModifiers())) {

			Class<?> clazz = currentClass();
			// System.out.println(currentClass().getName());

			throw new RuntimeException(clazz.getName()
					+ "A Message cannot be an Interface, Abstract OR an Enum");
			// test
		}

		String messageName = currentClass().getSimpleName();

		typeMap.put(currentClass(), getPath());

		builder.append(getTabs()).append(MESSAGE).append(SPACE)
				.append(messageName).append(OPEN_BLOCK).append(NEWLINE);

		tabDepth++;

		processFields();

		tabDepth--;

		builder.append(getTabs()).append(CLOSE_BLOCK).append(NEWLINE);

		return messageName;
	}

	private void processFields() {
		// WmsRuntimeSchema schema = (WmsRuntimeSchema) WmsRuntimeSchema
		// .getSchema(currentClass());
		//
		// Proto proto = currentClass().getAnnotation(Proto.class);
		//
		// for (int i = 1; i < schema.getFieldCount(); i++) {}

		Field[] fields = currentClass().getDeclaredFields();

		List<Field> fieldList = Arrays.asList(fields);

		if (order) {
			Collections.sort(fieldList,
					new Comparator<java.lang.reflect.Field>() {

						@Override
						public int compare(Field lField, Field rField) {

							if (lField.getName().hashCode() > rField.getName()
									.hashCode()) {
								return 1;
							} else if (lField.getName().hashCode() < rField
									.getName().hashCode()) {
								return -1;
							}
							return 0;
						}
					});
		}

		int i = 0;

		Proto proto = currentClass().getAnnotation(Proto.class);

		for (Field f : fieldList) {

			int mod = f.getModifiers();
			if (!Modifier.isPrivate(mod)) {// 添加，只把私有属性传输
				// Skip not private field
				continue;
			}
			// if(f.isAnnotationPresent(ProtoRequired.class)){
			// defaultOption= REQUIRED;
			// }
			i++;

			if (Modifier.isAbstract(mod) || Modifier.isTransient(mod)) {
				// Skip this field
				continue;
			}

			Class<?> fieldType = f.getType();

			// Primitives or Types we have come across before
			if (typeMap.containsKey(fieldType)) {
				if (null != proto && proto.required() > 0) {

					processField(REQUIRED, typeMap.get(fieldType), f.getName(),
							i);
				} else {

					processField(defaultOption, typeMap.get(fieldType),
							f.getName(), i);
				}
				continue;
			}

			if (fieldType.isEnum()) {
				processEnum(fieldType);
				processField(REQUIRED, typeMap.get(fieldType), f.getName(), i);
				continue;
			}

			if (Map.class.isAssignableFrom(fieldType)) {

				throw new IllegalArgumentException("protobuf不支持MAP");

			}

			if (Date.class.isAssignableFrom(fieldType)) {

				throw new IllegalArgumentException("protobuf不支持Date");

			}

			if (fieldType.isArray()) {
				Class<?> innerType = fieldType.getComponentType();

				if (!typeMap.containsKey(innerType)) {
					// TODO 不提倡在proto中使用消息嵌套
					// buildNestedType(innerType);
				}
				String typeName = getTypeName(innerType);
				processField(REPEATED, typeName, f.getName(), i);
				continue;
			}

			if (Collection.class.isAssignableFrom(fieldType)) {
				Class<?> innerType = null;

				Type t = f.getGenericType();

				if (t instanceof ParameterizedType) {
					ParameterizedType tt = (ParameterizedType) t;
					innerType = (Class<?>) tt.getActualTypeArguments()[0];
				}

				if (!typeMap.containsKey(innerType)) {
					// buildNestedType(innerType);
				}
				String typeName = getTypeName(innerType);
				processField(REPEATED, typeName, f.getName(), i);
				continue;
			}

			String typeName = getTypeName(fieldType);
			if (null != proto && proto.required() > 0) {
				processField(REQUIRED, typeName, f.getName(), i);
			} else {
				processField(defaultOption, typeName, f.getName(), i);
			}

		}
	}

	private String getTypeName(Class<?> fieldType) {
		String typeName = getPrimitivesMap().get(fieldType);
		if (null == typeName) {
			typeName = fieldType.getSimpleName();
		}
		return typeName;
	}

	private void buildNestedType(Class<?> type) {
		classStack.push(type);
		buildMessage();
		classStack.pop();
	}

	private void buildEntryType(String name, Class<?> innerType,
			Class<?> innerType2) {

		typeMap.put(currentClass(), getPath());

		builder.append(getTabs()).append(MESSAGE).append(SPACE).append(name)
				.append(OPEN_BLOCK).append(NEWLINE);

		tabDepth++;

		if (!typeMap.containsKey(innerType)) {
			buildNestedType(innerType);
			typeMap.remove(innerType);
			typeMap.put(innerType, getPath() + PATH_SEPERATOR + name
					+ PATH_SEPERATOR + innerType.getSimpleName());
		}
		processField(REQUIRED, typeMap.get(innerType), "key", 1);

		if (!typeMap.containsKey(innerType2)) {
			buildNestedType(innerType2);
			typeMap.remove(innerType2);
			typeMap.put(innerType2, getPath() + PATH_SEPERATOR + name
					+ PATH_SEPERATOR + innerType2.getSimpleName());
		}
		processField(REQUIRED, typeMap.get(innerType2), "value", 2);

		tabDepth--;

		builder.append(getTabs()).append(CLOSE_BLOCK).append(NEWLINE);
	}

	private void processEnum(Class<?> enumType) {

		classStack.push(enumType);
		typeMap.put(enumType, getPath());
		classStack.pop();

		builder.append(getTabs()).append(ENUM).append(SPACE)
				.append(enumType.getSimpleName()).append(OPEN_BLOCK)
				.append(NEWLINE);

		tabDepth++;

		int i = 0;
		for (Object e : enumType.getEnumConstants()) {
			builder.append(getTabs()).append(e.toString()).append(" = ")
					.append(i).append(LINE_END).append(NEWLINE);
		}

		tabDepth--;

		builder.append(getTabs()).append(CLOSE_BLOCK).append(NEWLINE);
	}

	@Override
	/**
	 * If the Proto file has not been generated, generate it. Then return it in string format.
	 * @return String - a String representing the proto file representing this class.
	 */
	public String toString() {
		if (builder == null) {
			generateProtoFile();
		}
		return builder.toString();
	}

}
