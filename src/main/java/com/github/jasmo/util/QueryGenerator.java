package com.github.jasmo.util;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * Utility to generate {@link com.github.jasmo.query.QueryUtil#query(Object, String)}
 */
public class QueryGenerator {

	public static void main(String[] args) {
		Set<Class> types = new LinkedHashSet<>();
		Collections.addAll(types, ClassNode.class, FieldNode.class, MethodNode.class, InnerClassNode.class);
		Collections.addAll(types, AnnotationNode.class, ParameterNode.class, TryCatchBlockNode.class, TypeAnnotationNode.class, Label.class);
		Collections.addAll(types,
				AbstractInsnNode.class, FieldInsnNode.class, FrameNode.class, IincInsnNode.class,
				InsnNode.class, IntInsnNode.class, InvokeDynamicInsnNode.class, JumpInsnNode.class,
				LabelNode.class, LdcInsnNode.class, LineNumberNode.class, LookupSwitchInsnNode.class,
				MethodInsnNode.class, MultiANewArrayInsnNode.class, TableSwitchInsnNode.class,
				TypeInsnNode.class, VarInsnNode.class);
		try {
			System.out.println("\tpublic static Object query(Object o, String key) {");
			for (Class type : types) {
				lookup(type);
			}
			System.out.println("\t\treturn null;");
			System.out.println("\t}");
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	private static void lookup(Class<?> type) throws ReflectiveOperationException {
		System.out.println("\t\tif (o instanceof " + type.getName() + ") {");
		System.out.println("\t\t\t" + type.getName() + " b = (" + type.getName() + ") o;");
		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			String accessor = "b." + field.getName();
			if ((field.getModifiers() & Modifier.PUBLIC) == 0) {
				try {
					String name = field.getName();
					if (name.equals("prev")) name = "previous";
					Method get;
					try {
						String getName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
						get = type.getDeclaredMethod(getName);
					} catch (NoSuchMethodException e) {
						String getName = "is" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
						get = type.getDeclaredMethod(getName);
					}
					if ((get.getModifiers() & Modifier.PUBLIC) == 0) {
						throw new ReflectiveOperationException();
					}
					if (get.getReturnType() != field.getType()) {
						throw new ReflectiveOperationException();
					}
					if (get.getParameterTypes().length != 0) {
						throw new ReflectiveOperationException();
					}
					accessor = "b." + get.getName() + "()";
				} catch (ReflectiveOperationException o) {
					accessor = "reflectField(o, \"" + field.getName() + "\")";
				}
			}
			if (Collection.class.isAssignableFrom(field.getType())) {
				accessor += ".toArray()";
			}
			field.setAccessible(true);
			if ((field.getModifiers() & Modifier.STATIC) == 0)
				System.out.println("\t\t\tif (key.equals(\"" + field.getName() + "\")) return " + accessor + ";");
		}
		System.out.println("\t\t}");
	}

}
