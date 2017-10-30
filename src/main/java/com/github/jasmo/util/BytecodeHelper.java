/*
 * Copyright © 2017 Caleb Whiting <caleb.andrew.whiting@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package com.github.jasmo.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public class BytecodeHelper {

	private static final Logger log = LogManager.getLogger("BytecodeHelper");

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
		log.debug("Applying mappings [");
		for (Map.Entry<String, String> entry : remap.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();
			if (k.equals(v))
				continue;
			// skip members with same name
			// field format =   [ "<owner>.<name>"          : "<newname>" ]
			// method format =  [ "<owner>.<name> <desc>"   : "<newname>" ]
			int n = k.indexOf('.');
			if (n != -1 && v.length() >= n && v.substring(n).equals(k)) {
				continue;
			}
			log.debug(" Map {} to {}", entry.getKey(), entry.getValue());
		}
		log.debug("]");
		SimpleRemapper remapper = new SimpleRemapper(remap);
		for (ClassNode node : new ArrayList<>(classMap.values())) {
			ClassNode copy = new ClassNode();
			ClassRemapper adapter = new ClassRemapper(copy, remapper);
			node.accept(adapter);
			classMap.put(node.name, copy);
		}
	}

}
