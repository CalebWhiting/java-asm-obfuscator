package com.github.jasmo.obfuscate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author Caleb Whiting
 */
public class RemoveDebugInfo implements Processor {

	private static final Logger log = LogManager.getLogger(RemoveDebugInfo.class);

	@Override
	public void process(Map<String, ClassNode> classMap) {
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
