/*
 * Copyright © 2017 Caleb Whiting <caleb.andrew.whiting@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package com.github.jasmo.obfuscate;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.github.jasmo.util.BytecodeHelper;

/**
 * @author Caleb Whiting
 */
public class ScrambleStrings implements Transformer {

	private static final Logger log = LogManager.getLogger("ScrambleStrings");

	private static final String FIELD_NAME = "string_store";
	private static final String CALL_NAME = "unscramble";
	private static final String CALL_DESC = "(I)Ljava/lang/String;";

	private ClassNode callOwner;
	private String[] strings;

	@Override
	public void transform(Map<String, ClassNode> classMap) {
		callOwner = (ClassNode) classMap.values().toArray()[new Random().nextInt(classMap.size())];
		log.debug("Adding unscramble method to {}.{}{}", callOwner.name, CALL_NAME, CALL_DESC);
		List<String> stringList = new ArrayList<>();
		for (ClassNode cn : classMap.values()) {
			for (MethodNode mn : cn.methods) {
				BytecodeHelper.forEach(mn.instructions, LdcInsnNode.class, ldc -> {
					// noinspection SuspiciousMethodCalls
					if (ldc.cst instanceof String && !stringList.contains(ldc.cst)) {
						stringList.add((String) ldc.cst);
					}
				});
			}
		}
		Collections.shuffle(stringList);
		strings = stringList.toArray(new String[stringList.size()]);
		for (ClassNode cn : classMap.values()) {
			cn.methods.forEach(mn -> scramble(cn, mn));
		}
		callOwner.methods.add(getUnscramble());
		callOwner.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, FIELD_NAME, "[Ljava/lang/String;", null, null));
		log.debug("Creating {} field containing {} strings", FIELD_NAME, stringList.size());
		try {
			createStaticConstructor(callOwner);
		} catch (Exception ex) {
			log.warn("Failed to transform strings", ex);
		}
	}

	private void scramble(ClassNode cn, MethodNode mn) {
		List<LdcInsnNode> ldcNodes = new LinkedList<>();
		BytecodeHelper.forEach(mn.instructions, LdcInsnNode.class, ldcNodes::add);
		for (LdcInsnNode node : ldcNodes) {
			if (node.cst instanceof String) {
				int index = indexOf(strings, node.cst);
				if (index == -1)
					continue;
				log.debug("Replacing string \"{}\" at {}.{}{} with {}.{}.{}", node.cst, cn.name, mn.name, mn.desc,
						callOwner.name, CALL_NAME, CALL_DESC);
				MethodInsnNode call = new MethodInsnNode(Opcodes.INVOKESTATIC, callOwner.name, CALL_NAME, CALL_DESC,
						false);
				mn.instructions.set(node, call);
				mn.instructions.insertBefore(call, getIntegerNode(index));
			}
		}
	}

	private MethodNode getUnscramble() {
		MethodNode mv = new MethodNode(ACC_PUBLIC | ACC_STATIC, CALL_NAME, CALL_DESC, null, null);
		mv.visitCode();
		mv.visitTypeInsn(NEW, "java/lang/String");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;");
		mv.visitFieldInsn(GETSTATIC, callOwner.name, FIELD_NAME, "[Ljava/lang/String;");
		mv.visitVarInsn(ILOAD, 0);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B");
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V");
		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		return mv;
	}

	private void createStaticConstructor(ClassNode owner) throws UnsupportedEncodingException {
		MethodNode original = BytecodeHelper.getMethod(owner, "<clinit>", "()V");
		MethodVisitor mv = owner.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
		// generate instructions
		InstructionAdapter builder = new InstructionAdapter(mv);
		builder.iconst(strings.length);
		builder.newarray(Type.getType(String.class));
		for (int i = 0; i < strings.length; i++) {
			builder.dup();
			builder.iconst(i);
			builder.aconst(Base64.getEncoder().encodeToString(strings[i].getBytes("UTF-8")));
			builder.astore(InstructionAdapter.OBJECT_TYPE);
		}
		builder.putstatic(callOwner.name, FIELD_NAME, "[Ljava/lang/String;");
		if (original != null) {
			// original should already end with RETURN
			owner.methods.remove(original);
			original.instructions.accept(builder);
		} else {
			builder.areturn(Type.VOID_TYPE);
		}
	}

	private void visitInteger(MethodVisitor mv, int i) {
		if (i >= -1 && i <= 5) {
			mv.visitInsn(Opcodes.ICONST_0 + i);
		} else {
			if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
				mv.visitIntInsn(Opcodes.BIPUSH, i);
			} else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
				mv.visitIntInsn(Opcodes.SIPUSH, i);
			} else {
				mv.visitLdcInsn(i);
			}
		}
	}

	private AbstractInsnNode getIntegerNode(int i) {
		if (i >= -1 && i <= 5) {
			return new InsnNode(Opcodes.ICONST_0 + i);
		} else {
			if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
				return new IntInsnNode(Opcodes.BIPUSH, i);
			} else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
				return new IntInsnNode(Opcodes.SIPUSH, i);
			} else {
				return new LdcInsnNode(i);
			}
		}
	}

	private int indexOf(Object[] array, Object value) {
		for (int i = 0; i < array.length; i++) {
			if (value == array[i] || value.equals(array[i])) {
				return i;
			}
		}
		return -1;
	}

}
