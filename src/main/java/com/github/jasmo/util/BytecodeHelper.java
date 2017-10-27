package com.github.jasmo.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.*;

import java.util.function.Consumer;

public class BytecodeHelper {
	private static final Logger log = LogManager.getLogger(BytecodeHelper.class);

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

}
