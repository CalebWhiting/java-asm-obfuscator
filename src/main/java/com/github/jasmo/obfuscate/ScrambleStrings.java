package com.github.jasmo.obfuscate;

import com.github.jasmo.util.BytecodeHelper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Caleb Whiting
 */
public class ScrambleStrings implements Processor {

	private static final String FIELD_NAME = "string_store";
	private static final String CALL_NAME = "unscramble";
	private static final String CALL_DESC = "(I)Ljava/lang/String;";

	private ClassNode callOwner;
	private String[] strings;

	@Override
	public void process(Map<String, ClassNode> classMap) {
		callOwner = (ClassNode) classMap.values().toArray()[new Random().nextInt(classMap.size())];
		System.out.println("String unscramble @ " + callOwner.name);
		List<String> stringList = new ArrayList<>();
		for (ClassNode cn : classMap.values()) {
			for (MethodNode mn : cn.methods) {
				BytecodeHelper.forEach(mn.instructions, LdcInsnNode.class, ldc -> {
					//noinspection SuspiciousMethodCalls
					if (ldc.cst instanceof String && !stringList.contains(ldc.cst)) {
						stringList.add((String) ldc.cst);
					}
				});
			}
		}
		Collections.shuffle(stringList);
		strings = stringList.toArray(new String[stringList.size()]);
		for (ClassNode cn : classMap.values()) {
			cn.methods.forEach(this :: scramble);
		}
		callOwner.methods.add(getUnscramble());
		callOwner.fields.add(new FieldNode(ACC_PUBLIC | ACC_STATIC, FIELD_NAME, "[Ljava/lang/String;", null, null));
		createClinit(callOwner);
	}

	private void scramble(MethodNode mn) {
		List<LdcInsnNode> ldcNodes = new LinkedList<>();
		BytecodeHelper.forEach(mn.instructions, LdcInsnNode.class, ldcNodes:: add);
		for (LdcInsnNode node : ldcNodes) {
			if (node.cst instanceof String) {
				int index = indexOf(strings, node.cst);
				if (index == -1)
					continue;
				MethodInsnNode call = new MethodInsnNode(Opcodes.INVOKESTATIC, callOwner.name, CALL_NAME, CALL_DESC, false);
				mn.instructions.set(node, call);
				mn.instructions.insertBefore(call, getIntegerNode(index));
			}
		}
	}

	public MethodNode getUnscramble() {
		MethodNode mv = new MethodNode(ACC_PUBLIC | ACC_STATIC, CALL_NAME, CALL_DESC, null, null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, callOwner.name, FIELD_NAME, "[Ljava/lang/String;");
		mv.visitVarInsn(ILOAD, 0);
		mv.visitInsn(AALOAD);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		return mv;
	}

	private MethodNode createClinit(ClassNode owner) {
		MethodNode clinit = BytecodeHelper.getMethod(owner, "<clinit>", "()V");
		if (clinit == null) {
			clinit = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
			owner.methods.add(clinit);
		}
		AbstractInsnNode last = clinit.instructions.getLast();
		if (last != null && last.getOpcode() == Opcodes.RETURN)
			clinit.instructions.remove(last);
		visitInteger(clinit, strings.length);
		clinit.visitTypeInsn(ANEWARRAY, "java/lang/String");
		for (int i = 0; i < strings.length; i++) {
			clinit.visitInsn(DUP);
			visitInteger(clinit, i);
			clinit.visitLdcInsn(strings[i]);
			clinit.visitInsn(AASTORE);
		}
		clinit.visitFieldInsn(PUTSTATIC, callOwner.name, FIELD_NAME, "[Ljava/lang/String;");
		clinit.visitInsn(RETURN);
		return clinit;
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
