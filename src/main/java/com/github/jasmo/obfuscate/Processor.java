package com.github.jasmo.obfuscate;

import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author Caleb Whiting
 */
public interface Processor {

	void process(Map<String, ClassNode> classMap);

}
