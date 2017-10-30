package com.github.jasmo.obfuscate;

import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author Caleb Whiting
 */
public interface Transformer {

	void transform(Map<String, ClassNode> classMap);

}
