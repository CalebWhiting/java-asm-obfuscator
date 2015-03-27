package com.revtek.rasmo.util;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * @author Caleb Whiting
 */
public class JRE {

	private final Map<String, ClassNode> classMap = new HashMap<>();
	private final Map<String, byte[]> files = new HashMap<>();

	public JRE() {
		String[] libraries = System.getProperty("java.class.path").split(";");
		for (String library : libraries) {
			if (!library.endsWith(".jar")) {
				continue;
			}
			try {
				JarFile jar = new JarFile(library);
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					try (InputStream in = jar.getInputStream(entry)) {
						byte[] bytes = IO.read(in);
						if (!entry.getName().endsWith(".class")) {
							files.put(entry.getName(), bytes);
							continue;
						}
						ClassNode c = new ClassNode();
						new ClassReader(bytes).accept(c, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
						classMap.put(c.name, c);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<String, ClassNode> getClassMap() {
		return classMap;
	}

	public Map<String, byte[]> getFiles() {
		return files;
	}

}
