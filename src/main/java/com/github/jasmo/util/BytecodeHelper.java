package com.github.jasmo.util;

import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public class BytecodeHelper {

	public static MethodNode getMethod(ClassNode node, String name, String desc) {
		return node.methods.stream()
		                   .filter(m -> m.name.equals(name))
		                   .filter(m -> m.desc.equals(desc))
		                   .findAny()
		                   .orElse(null);
	}

	public static FieldNode getField(ClassNode node, String name, String desc) {
		return node.fields.stream()
		                  .filter(m -> m.name.equals(name))
		                  .filter(m -> m.desc.equals(desc))
		                  .findAny()
		                  .orElse(null);
	}

	public static <T extends AbstractInsnNode> void forEach(InsnList instructions,
	                           Class<T> type,
	                           Consumer<T> consumer) {
		AbstractInsnNode[] array = instructions.toArray();
		for (AbstractInsnNode node : array) {
			if (node.getClass() == type) {
				//noinspection unchecked
				consumer.accept((T) node);
			}
		}
	}

	public static void forEach(InsnList instructions, Consumer<AbstractInsnNode> consumer) {
		forEach(instructions, AbstractInsnNode.class, consumer);
	}

	public static void applyMappings(Map<String, ClassNode> classMap, Map<String, String> remap) {
		SimpleRemapper remapper = new SimpleRemapper(remap);
		for (ClassNode node : new ArrayList<>(classMap.values())) {
			ClassNode copy = new ClassNode();
			ClassRemapper adapter = new ClassRemapper(copy, remapper);
			node.accept(adapter);
			classMap.put(node.name, copy);
		}
	}

}
