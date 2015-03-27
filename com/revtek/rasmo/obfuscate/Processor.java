package com.revtek.rasmo.obfuscate;

import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author Caleb Whiting
 */
public interface Processor {

	public void process(Map<String, ClassNode> classMap);

}
