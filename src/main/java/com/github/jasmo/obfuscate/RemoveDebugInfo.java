/*
 * Copyright © 2017 Caleb Whiting <caleb.andrew.whiting@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package com.github.jasmo.obfuscate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author Caleb Whiting
 */
public class RemoveDebugInfo implements Transformer {

	private static final Logger log = LogManager.getLogger("RemoveDebugInfo");

	@Override
	public void transform(Map<String, ClassNode> classMap) {
		Map<String, ClassNode> map = new HashMap<>();
		for (ClassNode cn : classMap.values()) {
			log.debug("Removing debug info from class: {}", cn.name);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			cn.accept(writer);
			ClassNode clone = new ClassNode();
			new ClassReader(writer.toByteArray()).accept(clone, ClassReader.SKIP_DEBUG);
			map.put(clone.name, clone);
		}
		classMap.clear();
		classMap.putAll(map);
	}

}
