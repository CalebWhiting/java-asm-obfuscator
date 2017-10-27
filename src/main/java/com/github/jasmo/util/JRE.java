package com.github.jasmo.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * @author Caleb Whiting
 */
public class JRE {

	private static final Logger log = LogManager.getLogger(JRE.class);

	private static JRE jre;
	private final Map<String, ClassNode> classMap = new HashMap<>();

	private JRE() {
		String[] libraries = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
		for (String library : libraries) {
			if (!library.endsWith(".jar")) {
				continue;
			}
			log.debug("Found JAR: {}", library);
			try {
				JarFile jar = new JarFile(library);
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					try (InputStream in = jar.getInputStream(entry)) {
						if (!entry.getName().endsWith(".class")) {
							continue;
						}
						byte[] bytes;
						try (ByteArrayOutputStream tmp = new ByteArrayOutputStream()) {
							byte[] buf = new byte[256];
							for (int n; (n = in.read(buf)) != -1; ) {
								tmp.write(buf, 0, n);
							}
							bytes = tmp.toByteArray();
						}
						ClassNode c = new ClassNode();
						new ClassReader(bytes).accept(c, 7);
						classMap.put(c.name, c);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static JRE getJRE() {
		if (jre == null)
			jre = new JRE();
		return jre;
	}

	public Map<String, ClassNode> getClassMap() {
		return classMap;
	}

}
