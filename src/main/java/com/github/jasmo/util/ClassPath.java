/*
 * Copyright © 2017 Caleb Whiting <caleb.andrew.whiting@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package com.github.jasmo.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Stream;

/**
 * @author Caleb Whiting
 *
 * Loads/caches jar files found in java.class.path as {@link ClassNode} objects and stores them in a map for conveneience.
 */
public class ClassPath extends HashMap<String, ClassNode> {

	private static final Logger log = LogManager.getLogger("ClassPath");

	private static ClassPath instance;

	private ClassPath() {
		String[] classpath = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
		Stream.of(classpath).filter(path -> path.endsWith(".jar")).forEach(this::append);
	}

	private void append(String jarPath) {
		log.debug("Loading library from classpath: {}", jarPath);
		try(JarFile jar = new JarFile(jarPath)) {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				try (InputStream in = jar.getInputStream(entry)) {
					if (entry.getName().endsWith(".class")) {
						byte[] bytes;
						try (ByteArrayOutputStream tmp = new ByteArrayOutputStream()) {
							byte[] buf = new byte[256];
							for (int n; (n = in.read(buf)) != -1; ) {
								tmp.write(buf, 0, n);
							}
							bytes = tmp.toByteArray();
						}
						ClassNode c = new ClassNode();
						ClassReader r = new ClassReader(bytes);
						r.accept(c, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
						put(c.name, c);
					}
				}
			}
		} catch (IOException e) {
			log.error("An error occurred while reading jar file: {}", jarPath, e);
		}
	}

	public static ClassPath getInstance() {
		if (instance == null)
			instance = new ClassPath();
		return instance;
	}

}
