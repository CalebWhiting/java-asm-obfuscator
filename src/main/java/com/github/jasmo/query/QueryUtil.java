package com.github.jasmo.query;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author Caleb Whiting
 */
public class QueryUtil {

	public static boolean isEqual(Object o, Object value) {
		if (value != null && value instanceof AnyOf) {
			for (Object ob : ((AnyOf) value).values()) {
				if (isEqual(o, ob))
					return true;
			}
			return false;
		}
		if ((o == null && value != null) || (value == null && o != null)) {
			return false;
		}
		if (o == value) {
			return true;
		}
		if (value.getClass().isArray() && o.getClass().isArray()) {
			int n = Array.getLength(value);
			if (n != Array.getLength(o))
				return false;
			Object[] array1 = new Object[n];
			Object[] array2 = new Object[n];
			for (int j = 0; j < n; j++) {
				array1[j] = Array.get(value, j);
				array2[j] = Array.get(o, j);
			}
			return Arrays.equals(array1, array2);
		}
		return value.equals(o);
	}

	public static boolean check(AbstractInsnNode node, Object[] values) {
		for (int i = 0; i < values.length; i += 2) {
			String key = (String) values[i];
			Object value = values[i + 1];
			Object o = query(node, key);
			if (!QueryUtil.isEqual(o, value))
				return false;
		}
		return true;
	}

	public static boolean check(AbstractInsnNode node, Query query) {
		return check(node, query.values());
	}

	private static Object reflectField(Object o, String name) {
		Class c = o.getClass();
		while (c != null) {
			try {
				Field field = c.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(o);
			} catch (ReflectiveOperationException e) {
			}
			c = c.getSuperclass();
		}
		return null;
	}

	public static Object query(Object o, String key) {
		if (o instanceof org.objectweb.asm.tree.ClassNode) {
			org.objectweb.asm.tree.ClassNode b = (org.objectweb.asm.tree.ClassNode) o;
			if (key.equals("version")) return b.version;
			if (key.equals("access")) return b.access;
			if (key.equals("name")) return b.name;
			if (key.equals("signature")) return b.signature;
			if (key.equals("superName")) return b.superName;
			if (key.equals("interfaces")) return b.interfaces.toArray();
			if (key.equals("sourceFile")) return b.sourceFile;
			if (key.equals("sourceDebug")) return b.sourceDebug;
			if (key.equals("module")) return b.module;
			if (key.equals("outerClass")) return b.outerClass;
			if (key.equals("outerMethod")) return b.outerMethod;
			if (key.equals("outerMethodDesc")) return b.outerMethodDesc;
			if (key.equals("visibleAnnotations")) return b.visibleAnnotations.toArray();
			if (key.equals("invisibleAnnotations")) return b.invisibleAnnotations.toArray();
			if (key.equals("visibleTypeAnnotations")) return b.visibleTypeAnnotations.toArray();
			if (key.equals("invisibleTypeAnnotations")) return b.invisibleTypeAnnotations.toArray();
			if (key.equals("attrs")) return b.attrs.toArray();
			if (key.equals("innerClasses")) return b.innerClasses.toArray();
			if (key.equals("fields")) return b.fields.toArray();
			if (key.equals("methods")) return b.methods.toArray();
		}
		if (o instanceof org.objectweb.asm.tree.FieldNode) {
			org.objectweb.asm.tree.FieldNode b = (org.objectweb.asm.tree.FieldNode) o;
			if (key.equals("access")) return b.access;
			if (key.equals("name")) return b.name;
			if (key.equals("desc")) return b.desc;
			if (key.equals("signature")) return b.signature;
			if (key.equals("value")) return b.value;
			if (key.equals("visibleAnnotations")) return b.visibleAnnotations.toArray();
			if (key.equals("invisibleAnnotations")) return b.invisibleAnnotations.toArray();
			if (key.equals("visibleTypeAnnotations")) return b.visibleTypeAnnotations.toArray();
			if (key.equals("invisibleTypeAnnotations")) return b.invisibleTypeAnnotations.toArray();
			if (key.equals("attrs")) return b.attrs.toArray();
		}
		if (o instanceof org.objectweb.asm.tree.MethodNode) {
			org.objectweb.asm.tree.MethodNode b = (org.objectweb.asm.tree.MethodNode) o;
			if (key.equals("access")) return b.access;
			if (key.equals("name")) return b.name;
			if (key.equals("desc")) return b.desc;
			if (key.equals("signature")) return b.signature;
			if (key.equals("exceptions")) return b.exceptions.toArray();
			if (key.equals("parameters")) return b.parameters.toArray();
			if (key.equals("visibleAnnotations")) return b.visibleAnnotations.toArray();
			if (key.equals("invisibleAnnotations")) return b.invisibleAnnotations.toArray();
			if (key.equals("visibleTypeAnnotations")) return b.visibleTypeAnnotations.toArray();
			if (key.equals("invisibleTypeAnnotations")) return b.invisibleTypeAnnotations.toArray();
			if (key.equals("attrs")) return b.attrs.toArray();
			if (key.equals("annotationDefault")) return b.annotationDefault;
			if (key.equals("visibleParameterAnnotations")) return b.visibleParameterAnnotations;
			if (key.equals("invisibleParameterAnnotations")) return b.invisibleParameterAnnotations;
			if (key.equals("instructions")) return b.instructions;
			if (key.equals("tryCatchBlocks")) return b.tryCatchBlocks.toArray();
			if (key.equals("maxStack")) return b.maxStack;
			if (key.equals("maxLocals")) return b.maxLocals;
			if (key.equals("localVariables")) return b.localVariables.toArray();
			if (key.equals("visibleLocalVariableAnnotations")) return b.visibleLocalVariableAnnotations.toArray();
			if (key.equals("invisibleLocalVariableAnnotations")) return b.invisibleLocalVariableAnnotations.toArray();
			if (key.equals("visited")) return reflectField(o, "visited");
		}
		if (o instanceof org.objectweb.asm.tree.InnerClassNode) {
			org.objectweb.asm.tree.InnerClassNode b = (org.objectweb.asm.tree.InnerClassNode) o;
			if (key.equals("name")) return b.name;
			if (key.equals("outerName")) return b.outerName;
			if (key.equals("innerName")) return b.innerName;
			if (key.equals("access")) return b.access;
		}
		if (o instanceof org.objectweb.asm.tree.AnnotationNode) {
			org.objectweb.asm.tree.AnnotationNode b = (org.objectweb.asm.tree.AnnotationNode) o;
			if (key.equals("desc")) return b.desc;
			if (key.equals("values")) return b.values.toArray();
		}
		if (o instanceof org.objectweb.asm.tree.ParameterNode) {
			org.objectweb.asm.tree.ParameterNode b = (org.objectweb.asm.tree.ParameterNode) o;
			if (key.equals("name")) return b.name;
			if (key.equals("access")) return b.access;
		}
		if (o instanceof org.objectweb.asm.tree.TryCatchBlockNode) {
			org.objectweb.asm.tree.TryCatchBlockNode b = (org.objectweb.asm.tree.TryCatchBlockNode) o;
			if (key.equals("start")) return b.start;
			if (key.equals("end")) return b.end;
			if (key.equals("handler")) return b.handler;
			if (key.equals("type")) return b.type;
			if (key.equals("visibleTypeAnnotations")) return b.visibleTypeAnnotations.toArray();
			if (key.equals("invisibleTypeAnnotations")) return b.invisibleTypeAnnotations.toArray();
		}
		if (o instanceof org.objectweb.asm.tree.TypeAnnotationNode) {
			org.objectweb.asm.tree.TypeAnnotationNode b = (org.objectweb.asm.tree.TypeAnnotationNode) o;
			if (key.equals("typeRef")) return b.typeRef;
			if (key.equals("typePath")) return b.typePath;
		}
		if (o instanceof org.objectweb.asm.Label) {
			org.objectweb.asm.Label b = (org.objectweb.asm.Label) o;
			if (key.equals("info")) return b.info;
			if (key.equals("status")) return reflectField(o, "status");
			if (key.equals("line")) return reflectField(o, "line");
			if (key.equals("position")) return reflectField(o, "position");
			if (key.equals("referenceCount")) return reflectField(o, "referenceCount");
			if (key.equals("srcAndRefPositions")) return reflectField(o, "srcAndRefPositions");
			if (key.equals("inputStackTop")) return reflectField(o, "inputStackTop");
			if (key.equals("outputStackMax")) return reflectField(o, "outputStackMax");
			if (key.equals("frame")) return reflectField(o, "frame");
			if (key.equals("successor")) return reflectField(o, "successor");
			if (key.equals("successors")) return reflectField(o, "successors");
			if (key.equals("next")) return reflectField(o, "next");
		}
		if (o instanceof org.objectweb.asm.tree.AbstractInsnNode) {
			org.objectweb.asm.tree.AbstractInsnNode b = (org.objectweb.asm.tree.AbstractInsnNode) o;
			if (key.equals("opcode")) return b.getOpcode();
			if (key.equals("visibleTypeAnnotations")) return b.visibleTypeAnnotations.toArray();
			if (key.equals("invisibleTypeAnnotations")) return b.invisibleTypeAnnotations.toArray();
			if (key.equals("prev")) return b.getPrevious();
			if (key.equals("next")) return b.getNext();
			if (key.equals("index")) return reflectField(o, "index");
		}
		if (o instanceof org.objectweb.asm.tree.FieldInsnNode) {
			org.objectweb.asm.tree.FieldInsnNode b = (org.objectweb.asm.tree.FieldInsnNode) o;
			if (key.equals("owner")) return b.owner;
			if (key.equals("name")) return b.name;
			if (key.equals("desc")) return b.desc;
		}
		if (o instanceof org.objectweb.asm.tree.FrameNode) {
			org.objectweb.asm.tree.FrameNode b = (org.objectweb.asm.tree.FrameNode) o;
			if (key.equals("type")) return b.type;
			if (key.equals("local")) return b.local.toArray();
			if (key.equals("stack")) return b.stack.toArray();
		}
		if (o instanceof org.objectweb.asm.tree.IincInsnNode) {
			org.objectweb.asm.tree.IincInsnNode b = (org.objectweb.asm.tree.IincInsnNode) o;
			if (key.equals("var")) return b.var;
			if (key.equals("incr")) return b.incr;
		}
		if (o instanceof org.objectweb.asm.tree.InsnNode) {
			org.objectweb.asm.tree.InsnNode b = (org.objectweb.asm.tree.InsnNode) o;
		}
		if (o instanceof org.objectweb.asm.tree.IntInsnNode) {
			org.objectweb.asm.tree.IntInsnNode b = (org.objectweb.asm.tree.IntInsnNode) o;
			if (key.equals("operand")) return b.operand;
		}
		if (o instanceof org.objectweb.asm.tree.InvokeDynamicInsnNode) {
			org.objectweb.asm.tree.InvokeDynamicInsnNode b = (org.objectweb.asm.tree.InvokeDynamicInsnNode) o;
			if (key.equals("name")) return b.name;
			if (key.equals("desc")) return b.desc;
			if (key.equals("bsm")) return b.bsm;
			if (key.equals("bsmArgs")) return b.bsmArgs;
		}
		if (o instanceof org.objectweb.asm.tree.JumpInsnNode) {
			org.objectweb.asm.tree.JumpInsnNode b = (org.objectweb.asm.tree.JumpInsnNode) o;
			if (key.equals("label")) return b.label;
		}
		if (o instanceof org.objectweb.asm.tree.LabelNode) {
			org.objectweb.asm.tree.LabelNode b = (org.objectweb.asm.tree.LabelNode) o;
			if (key.equals("label")) return b.getLabel();
		}
		if (o instanceof org.objectweb.asm.tree.LdcInsnNode) {
			org.objectweb.asm.tree.LdcInsnNode b = (org.objectweb.asm.tree.LdcInsnNode) o;
			if (key.equals("cst")) return b.cst;
		}
		if (o instanceof org.objectweb.asm.tree.LineNumberNode) {
			org.objectweb.asm.tree.LineNumberNode b = (org.objectweb.asm.tree.LineNumberNode) o;
			if (key.equals("line")) return b.line;
			if (key.equals("start")) return b.start;
		}
		if (o instanceof org.objectweb.asm.tree.LookupSwitchInsnNode) {
			org.objectweb.asm.tree.LookupSwitchInsnNode b = (org.objectweb.asm.tree.LookupSwitchInsnNode) o;
			if (key.equals("dflt")) return b.dflt;
			if (key.equals("keys")) return b.keys.toArray();
			if (key.equals("labels")) return b.labels.toArray();
		}
		if (o instanceof org.objectweb.asm.tree.MethodInsnNode) {
			org.objectweb.asm.tree.MethodInsnNode b = (org.objectweb.asm.tree.MethodInsnNode) o;
			if (key.equals("owner")) return b.owner;
			if (key.equals("name")) return b.name;
			if (key.equals("desc")) return b.desc;
			if (key.equals("itf")) return b.itf;
		}
		if (o instanceof org.objectweb.asm.tree.MultiANewArrayInsnNode) {
			org.objectweb.asm.tree.MultiANewArrayInsnNode b = (org.objectweb.asm.tree.MultiANewArrayInsnNode) o;
			if (key.equals("desc")) return b.desc;
			if (key.equals("dims")) return b.dims;
		}
		if (o instanceof org.objectweb.asm.tree.TableSwitchInsnNode) {
			org.objectweb.asm.tree.TableSwitchInsnNode b = (org.objectweb.asm.tree.TableSwitchInsnNode) o;
			if (key.equals("min")) return b.min;
			if (key.equals("max")) return b.max;
			if (key.equals("dflt")) return b.dflt;
			if (key.equals("labels")) return b.labels.toArray();
		}
		if (o instanceof org.objectweb.asm.tree.TypeInsnNode) {
			org.objectweb.asm.tree.TypeInsnNode b = (org.objectweb.asm.tree.TypeInsnNode) o;
			if (key.equals("desc")) return b.desc;
		}
		if (o instanceof org.objectweb.asm.tree.VarInsnNode) {
			org.objectweb.asm.tree.VarInsnNode b = (org.objectweb.asm.tree.VarInsnNode) o;
			if (key.equals("var")) return b.var;
		}
		return null;
	}

}