package com.revtek.rasmo.analyze;

import org.objectweb.asm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author Caleb Whiting
 */
public class BytecodePrinter extends ClassVisitor {

	private final PrintStream writer;

	public int tab;

	private static final Map<Integer, String> OPCODES;

	static {
		OPCODES = new HashMap<>();
		Field[] fields = Opcodes.class.getFields();
		for (int i = 0, j = 0; i < fields.length; i++) {
			Field f = fields[i];
			if (f.getName().equals("NOP"))
				j++;
			if (j == 0)
				continue;
			try {
				int value = (int) f.get(null);
				OPCODES.put(value, f.getName());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static String toText(ClassNode node) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try (PrintStream out = new PrintStream(buffer)) {
			BytecodePrinter printer = new BytecodePrinter(out);
			node.accept(printer);
		}
		return new String(buffer.toByteArray());
	}

	public BytecodePrinter(PrintStream writer) {
		super(Opcodes.ASM5);
		this.writer = writer;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		int separator = name.lastIndexOf('/');
		String pkg = null;
		if (separator != -1) {
			pkg = name.substring(0, separator).replace('/', '.');
			name = name.substring(separator + 1);
		}
		if (pkg != null) {
			println("package ", pkg, ";");
			println();
		}
		String extend = superName == null || superName.equals("java/lang/Object") ?
		                "" : " extends " + superName;
		String implement = "";
		if (interfaces != null && interfaces.length > 0) {
			implement = " implements ";
			for (int i = 0; i < interfaces.length; i++) {
				String iface = interfaces[i];
				implement += iface;
				if (i < interfaces.length - 1) implement += ", ";
			}
		}
		String signatureText = signature == null ? "" :
		                       "/* Signature: " + signature + " */";
		if (signatureText.length() > 0)
			println(signatureText);
		String mod = access(access);
		if (!mod.contains("interface"))
			mod += "class ";
		println(mod, name, extend, implement, " {");
		println();
		tab++;
	}

	private String access(int access) {
		String s = Modifier.toString(access);
		return s.length() > 0 ? s + " " : "";
	}

	@Override
	public void visitSource(String source, String debug) {
		println("/* Source: ", source, ", Debug: ", debug, " */");
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		println("/* Outer-Class: ", owner, ".", name, desc, " */");
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		println("/* Annotation: ", desc, " -> ", visible, " */");
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		println("/* Type-Annotation: ", typeRef, " -> ", typePath, " -> ", desc, " -> ", visible, " */");
		return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitAttribute(Attribute attr) {
		println("/* Attribute: ", attr, " */");
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		println("/* Inner-Class: ", name, ", ", outerName, ", ", innerName, ", ", access, " */");
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		String valueText = value == null ? "" :
		                   " = " + value;
		String signatureText = signature == null ? "" :
		                       " /* Signature: " + signature + " */";
		try {
			println(access(access), " ", Type.getType(desc).getInternalName(), " ", name, valueText, ";", signatureText);
		} catch (Exception e) {
			println(access(access), " ", Type.getType(desc).getClassName(), " ", name, valueText, ";", signatureText);
		}
		return new FieldPrinter();
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		String exceptionsText = "";
		if (exceptions != null && exceptions.length > 0) {
			exceptionsText = " throws ";
			for (int i = 0; i < exceptions.length; i++) {
				String iface = exceptions[i];
				exceptionsText += iface;
				if (i < exceptions.length - 1) exceptionsText += ", ";
			}
		}
		String descText = "";
		Type ret = Type.getReturnType(desc);
		descText += "(";
		Type[] args = Type.getArgumentTypes(desc);
		for (int i = 0; i < args.length; i++) {
			Type arg = args[i];
			descText += arg.getClassName();
			descText += " arg" + i;
			if (i < args.length - 1)
				descText += ", ";
		}
		descText += ")";
		String signatureText = signature == null ? "" :
		                       " /* Signature: " + signature + " */";
		println();
		println(access(access), ret.getClassName(), " ", name, " ", descText, exceptionsText, signatureText);
		return new MethodPrinter();
	}

	@Override
	public void visitEnd() {
		tab--;
		println("}");
	}

	private void println(Object... chunks) {
		String s = "";
		for (int i = 0; i < tab; i++)
			s += '\t';
		for (Object chunk : chunks)
			s += chunk;
		writer.println(s);
	}

	public MethodVisitor getMethodPrinter() {
		return new MethodPrinter();
	}

	public FieldVisitor getFieldPrinter() {
		return new FieldPrinter();
	}

	private class MethodPrinter extends MethodVisitor {

		public MethodPrinter() {
			super(Opcodes.ASM5);
		}

		@Override
		public void visitCode() {
			println("{");
			tab++;
		}

		@Override
		public void visitInsn(int opcode) {
			println(getOpcodeName(opcode));
		}

		@Override
		public void visitIntInsn(int opcode, int operand) {
			println(getOpcodeName(opcode), " '", operand, "'");
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
			println(getOpcodeName(opcode), " ", var);
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
			println(getOpcodeName(opcode), " '", type, "'");
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			println(getOpcodeName(opcode), " ", owner, ".", name, " ", desc);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			println(getOpcodeName(opcode), " ", owner, ".", name, desc);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
			println("INVOKEDYNAMIC ", name, ", ", desc, ", ", bsm, ", ", Arrays.toString(bsmArgs));
		}

		@Override
		public void visitJumpInsn(int opcode, Label label) {
			println(getOpcodeName(opcode), " ", label);
		}

		@Override
		public void visitLabel(Label label) {
			println("Label ", label);
		}

		@Override
		public void visitLdcInsn(Object cst) {
			println("LDC ", cst);
		}

		@Override
		public void visitIincInsn(int var, int increment) {
			println("IINC var ", var, " by ", increment);
		}

		@Override
		public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
			println("TABLESWITCH ", min, " -> ", max, ", ", dflt, ", ", Arrays.toString(labels));
		}

		@Override
		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
			println("LOOKUPSWITCH ", dflt, ", ", Arrays.toString(keys), ", ", Arrays.toString(labels));
		}

		@Override
		public void visitMultiANewArrayInsn(String desc, int dims) {
			println("MULTIANEWARRAY ", desc, ", ", dims);
		}

		@Override
		public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			println("/* Insn-Annotation: ", typeRef, " -> ", typePath, " -> ", desc, " -> ", visible, " */");
			return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			println("/* Try-Catch-Block: ", start, " -> ", end, " -> ", handler, ", ", type, " */");
			super.visitTryCatchBlock(start, end, handler, type);
		}

		@Override
		public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			println("/* Try-Catch-Annotation: ", typeRef, " -> ", typePath, " -> ", desc, " -> ", visible, " */");
			return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			println("/* Local Variable: ", name, ", ", desc, ", ", signature, ", ", start, ", ", end, ", ", index, " */");
		}

		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
			println("/* Local-Variable-Annotation: ", typeRef, " -> ", typePath, " -> ", desc, " -> ", visible, " */");
			return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
		}

		@Override
		public void visitLineNumber(int line, Label start) {
			println("Line ", line, ", ", start);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			println("/* Max's: { Max-Stack: ", maxStack, ", Max-Locals: ", maxLocals, " } */");
		}

		@Override
		public void visitEnd() {
			tab--;
			println("}");
		}

		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
			println("Frame ", type, ", ", nLocal, ", ", Arrays.toString(local), ", ", nStack, ", ", Arrays.toString(stack));
		}

		@Override
		public void visitAttribute(Attribute attr) {
			println("/* Attribute: ", attr, " */");
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			println("/* Parameter-Annotation: ", parameter, ", ", desc, ", ", visible, " */");
			return super.visitParameterAnnotation(parameter, desc, visible);
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			println("/* Type-Annotation: ", typeRef, " -> ", typePath, " -> ", desc, " -> ", visible, " */");
			return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			println("/* Annotation: ", desc, " -> ", visible, " */");
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return super.visitAnnotationDefault();
		}

		@Override
		public void visitParameter(String name, int access) {
			println("/* Parameter: ", name, ", ", access, " */");
		}

	}

	private class FieldPrinter extends FieldVisitor {

		public FieldPrinter() {
			super(Opcodes.ASM5);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			println("/* Annotation: ", desc, " -> ", visible, " */");
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			println("/* Type-Annotation: ", typeRef, " -> ", typePath, " -> ", desc, " -> ", visible, " */");
			return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
		}

		@Override
		public void visitAttribute(Attribute attr) {
			println("/* Attribute: ", attr, " */");
		}

		@Override
		public void visitEnd() {
			super.visitEnd();
		}

	}

	private String getOpcodeName(int opcode) {
		return OPCODES.get(opcode);
	}

}
