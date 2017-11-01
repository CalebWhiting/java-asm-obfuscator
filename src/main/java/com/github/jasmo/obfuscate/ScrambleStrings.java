/*
 * Copyright © 2017 Caleb Whiting <caleb.andrew.whiting@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package com.github.jasmo.obfuscate;

import com.github.jasmo.util.BytecodeHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Caleb Whiting
 */
public class ScrambleStrings implements Transformer {

	private static final Logger log = LogManager.getLogger("ScrambleStrings");

	private static final String FIELD_NAME = "string_store";
	private static final String CALL_NAME = "unscramble";
	private static final String CALL_DESC = "(I)Ljava/lang/String;";

	private ClassNode unscrambleClass;
	private List<String> stringList;

	@Override
	public void transform(Map<String, ClassNode> classMap) {
		stringList = new ArrayList<>();
		do {
			unscrambleClass = (ClassNode) classMap.values().toArray()[new Random().nextInt(classMap.size())];
		} while ((unscrambleClass.access & Opcodes.ACC_INTERFACE) != 0);
		// Build string list
		log.debug("Building string list");
		classMap.values().stream().flatMap(cn -> cn.methods.stream()).forEach(this::buildStringList);
		Collections.shuffle(stringList);
		// Replace LDC constants with calls to unscramble
		log.debug("Scrambling LDC constants");
		classMap.values().forEach(cn -> cn.methods.forEach(mn -> scramble(cn, mn)));
		// Add unscrambling handler
		log.debug("Creating {} field containing {} strings", FIELD_NAME, stringList.size());
		unscrambleClass.visitField(ACC_PUBLIC | ACC_STATIC, FIELD_NAME, "[Ljava/lang/String;", null, null);
		log.debug("Adding unscramble method to {}.{}{}", unscrambleClass.name, CALL_NAME, CALL_DESC);
		createUnscramble();
		try {
			createStaticConstructor(unscrambleClass);
		} catch (Exception ex) {
			log.warn("Failed to transform strings", ex);
		}
	}

	private void buildStringList(MethodNode mn) {
		BytecodeHelper.forEach(mn.instructions, LdcInsnNode.class, ldc -> {
			if (ldc.cst instanceof String && !stringList.contains(ldc.cst)) {
				stringList.add((String) ldc.cst);
			}
		});
	}

	private void scramble(ClassNode cn, MethodNode mn) {
		List<LdcInsnNode> ldcNodes = new LinkedList<>();
		BytecodeHelper.forEach(mn.instructions, LdcInsnNode.class, ldcNodes::add);
		for (LdcInsnNode node : ldcNodes) {
			if (node.cst instanceof String) {
				int index = stringList.indexOf(node.cst);
				if (index == -1)
					continue;
				log.debug("Replacing string constant \"{}\" at {}.{}{}", node.cst, cn.name, mn.name, mn.desc);
				MethodInsnNode call = new MethodInsnNode(Opcodes.INVOKESTATIC, unscrambleClass.name, CALL_NAME, CALL_DESC, false);
				mn.instructions.set(node, call);
				mn.instructions.insertBefore(call, BytecodeHelper.newIntegerNode(index));
			}
		}
	}
	
	private void createUnscramble() {
		MethodVisitor mv = unscrambleClass.visitMethod(ACC_PUBLIC | ACC_STATIC, CALL_NAME, CALL_DESC, null, null);
		mv.visitCode();
		mv.visitTypeInsn(NEW, "java/lang/String");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;", false);
		mv.visitFieldInsn(GETSTATIC, unscrambleClass.name, FIELD_NAME, "[Ljava/lang/String;");
		mv.visitVarInsn(ILOAD, 0);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "(Ljava/lang/String;)[B", false);
		mv.visitLdcInsn("UTF-8");
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/lang/String;)V", false);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void createStaticConstructor(ClassNode owner) throws UnsupportedEncodingException {
		MethodNode original = BytecodeHelper.getMethod(owner, "<clinit>", "()V");
		MethodVisitor mv = owner.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
		// generate instructions
		InstructionAdapter builder = new InstructionAdapter(mv);
		builder.iconst(stringList.size());
		builder.newarray(Type.getType(String.class));
		for (int i = 0; i < stringList.size(); i++) {
			builder.dup();
			builder.iconst(i);
			builder.aconst(Base64.getEncoder().encodeToString(stringList.get(i).getBytes("UTF-8")));
			builder.astore(InstructionAdapter.OBJECT_TYPE);
		}
		builder.putstatic(unscrambleClass.name, FIELD_NAME, "[Ljava/lang/String;");
		// merge with original if it exists
		if (original != null) {
			// original should already end with RETURN
			owner.methods.remove(original);
			original.instructions.accept(builder);
		} else {
			builder.areturn(Type.VOID_TYPE);
		}
	}

}
