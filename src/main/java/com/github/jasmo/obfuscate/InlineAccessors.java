/*
 * Copyright © 2017 Caleb Whiting <caleb.andrew.whiting@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package com.github.jasmo.obfuscate;

import com.github.jasmo.query.Query;
import com.github.jasmo.query.QueryUtil;
import com.github.jasmo.util.BytecodeHelper;
import com.github.jasmo.util.ClassPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Caleb Whiting
 */
public class InlineAccessors implements Transformer {

	private static final Logger log = LogManager.getLogger("InlineAccessors");

	private Map<String, ClassNode> classMap;

	@Override
	public void transform(Map<String, ClassNode> classMap) {
		this.classMap = classMap;
		for (ClassNode node : new ArrayList<>(classMap.values())) {
			for (FieldNode field : node.fields) {
				for (MethodNode method : new ArrayList<>(node.methods)) {
					if (isGetterFor(node, field, method)) {
						node.methods.remove(method);
						log.debug("Inlining getter {}.{}{}", node.name, method.name, method.desc);
						replace(Opcodes.GETFIELD, node, field, method);
					}
					if (isSetterFor(node, field, method)) {
						node.methods.remove(method);
						log.debug("Inlining setter {}.{}{}", node.name, method.name, method.desc);
						replace(Opcodes.PUTFIELD, node, field, method);
					}
				}
			}
		}
	}

	private void replace(int opcode, ClassNode owner, FieldNode field, MethodNode m) {
		if (Modifier.isStatic(field.access))
			opcode -= 2;
		for (ClassNode cn : classMap.values()) {
			for (MethodNode mn : cn.methods) {
				AbstractInsnNode[] instructions = mn.instructions.toArray();
				for (AbstractInsnNode node : instructions) {
					if (node.getType() != AbstractInsnNode.METHOD_INSN) {
						continue;
					}
					MethodInsnNode min = (MethodInsnNode) node;
					List<String> owners = getChildNames(owner);
					if (owners.contains(min.owner) && min.name.equals(m.name) && min.desc.equals(m.desc)) {
						FieldInsnNode fin = new FieldInsnNode(opcode, min.owner, field.name, field.desc);
						log.debug(" replace {}.{}.{}, insn: {}",
								cn.name, mn.name, mn.desc, QueryUtil.query(fin, "index"));
						mn.instructions.set(min, fin);
					}
				}
			}
		}
	}

	private List<String> getChildNames(ClassNode owner) {
		List<String> owners = new ArrayList<>();
		Stack<ClassNode> stack = new Stack<>();
		stack.add(owner);
		while (stack.size() > 0) {
			ClassNode c = stack.pop();
			owners.add(c.name);
			classMap.values().stream().filter(check ->
					check.superName.equals(c.name)).forEach(stack:: push);
		}
		return owners;
	}

	private boolean isGetterFor(ClassNode owner, FieldNode field, MethodNode method) {
		if (local(method.access) == local(field.access) && isTopLevel(owner, method)) {
			Type type = Type.getType(field.desc);
			Type getType = Type.getMethodType(type);
			Type methodType = Type.getMethodType(method.desc);
			if (methodType.equals(getType)) {
				List<AbstractInsnNode> instructions = getRealInstructions(method);
				Query[] queries = getPattern(true, owner, field);
				return matches(instructions, queries);
			}
		}
		return false;
	}

	private boolean isSetterFor(ClassNode owner, FieldNode field, MethodNode method) {
		if (local(method.access) == local(field.access) && isTopLevel(owner, method)) {
			Type type = Type.getType(field.desc);
			Type setType = Type.getMethodType(Type.VOID_TYPE, type);
			Type methodType = Type.getMethodType(method.desc);
			if (methodType.equals(setType)) {
				List<AbstractInsnNode> instructions = getRealInstructions(method);
				Query[] queries = getPattern(false, owner, field);
				return matches(instructions, queries);
			}
		}
		return false;
	}

	private Query[] getPattern(boolean get, ClassNode owner, FieldNode field) {
		Type type = Type.getType(field.desc);
		List<Query> queries = new LinkedList<>();
		boolean local = local(field.access);
		if (local)
			queries.add(new Query("opcode", Opcodes.ALOAD, "var", 0));
		if (get) {
			int opcode = local ? Opcodes.GETFIELD : Opcodes.GETSTATIC;
			queries.add(new Query("opcode", opcode, "owner", owner.name, "name", field.name, "desc", field.desc));
			queries.add(new Query("opcode", type.getOpcode(Opcodes.IRETURN)));
		} else {
			int opcode = local ? Opcodes.PUTFIELD : Opcodes.PUTSTATIC;
			queries.add(new Query("opcode", type.getOpcode(Opcodes.ILOAD), "var", 0));
			queries.add(new Query("opcode", opcode, "owner", owner.name, "name", field.name, "desc", field.desc));
			queries.add(new Query("opcode", Opcodes.RETURN));
		}
		return queries.toArray(new Query[queries.size()]);
	}

	private boolean matches(List<AbstractInsnNode> list, Query... queries) {
		if (list.size() != queries.length) return false;
		for (int i = 0; i < list.size(); i++) {
			AbstractInsnNode node = list.get(i);
			/*if (!node.check(queries[i].values())) {
				return false;
			}*/
			if (!QueryUtil.check(node, queries[i].values())) {
				return false;
			}
		}
		return true;
	}

	private boolean isTopLevel(ClassNode owner, MethodNode method) {
		Stack<ClassNode> stack = new Stack<>();
		stack.add(owner);
		while (stack.size() > 0) {
			ClassNode node = stack.pop();
			if (node != owner && BytecodeHelper.getMethod(node, method.name, method.desc) != null)
				return false;
			ClassNode superClass = getClass(node.superName);
			if (superClass != null)
				stack.push(superClass);
			if (node.interfaces != null) {
				node.interfaces.forEach(iface -> {
					ClassNode interfaceClass = getClass(iface);
					if (interfaceClass != null)
						stack.push(interfaceClass);
				});
			}
		}
		return true;
	}

	private ClassNode getClass(String name) {
		if (name == null)
			return null;
		ClassNode c = classMap.get(name);
		if (c != null)
			return c;
		return ClassPath.getInstance().get(name);
	}

	private List<AbstractInsnNode> getRealInstructions(MethodNode method) {
		List<AbstractInsnNode> instructions = new LinkedList<>();
		for (AbstractInsnNode node : method.instructions.toArray()) {
			if (node.getOpcode() != -1)
				instructions.add(node);
		}
		return instructions;
	}

	private boolean local(int access) {
		return (access & Opcodes.ACC_STATIC) == 0;
	}

}
