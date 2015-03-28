package com.revtek.rasmo.obfuscate;

import com.revtek.rasmo.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * @author Caleb Whiting
 */
public class Obfuscator {

	private final Map<String, ClassNode> classMap = new HashMap<>();
	private final Map<String, byte[]> files = new HashMap<>();

	private int readFlags = ClassReader.SKIP_DEBUG | ClassReader.EXPAND_FRAMES;
	private int writeFlags = ClassWriter.COMPUTE_MAXS;

	public void supply(JarFile jar) {
		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			try (InputStream in = jar.getInputStream(entry)) {
				byte[] bytes = IO.read(in);
				if (!entry.getName().endsWith(".class")) {
					getFiles().put(entry.getName(), bytes);
					continue;
				}
				ClassNode c = new ClassNode();
				new ClassReader(bytes).accept(c, getReadFlags());
				getClassMap().put(c.name, c);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void apply(Processor processor) {
		processor.process(getClassMap());
		// re-populate class map, so that any changed names are registered
		List<ClassNode> values = new ArrayList<>(getClassMap().values());
		getClassMap().clear();
		for (ClassNode cn : values)
			getClassMap().put(cn.name, cn);
	}

	public void write(JarOutputStream out) throws IOException {
		for (ClassNode node : getClassMap().values()) {
			JarEntry entry = new JarEntry(node.name + ".class");
			out.putNextEntry(entry);
			ClassWriter writer = new ClassWriter(getWriteFlags());
			node.accept(writer);
			out.write(writer.toByteArray());
			out.closeEntry();
		}
		for (Map.Entry<String, byte[]> entry : getFiles().entrySet()) {
			out.putNextEntry(new JarEntry(entry.getKey()));
			out.write(entry.getValue());
			out.closeEntry();
		}
	}

	public void setReadFlags(int flags) {
		this.readFlags = flags;
	}

	public int getReadFlags() {
		return readFlags;
	}

	public void setWriteFlags(int writeFlags) {
		this.writeFlags = writeFlags;
	}

	public int getWriteFlags() {
		return writeFlags;
	}

	public Map<String, byte[]> getFiles() {
		return files;
	}

	public Map<String, ClassNode> getClassMap() {
		return classMap;
	}

}
