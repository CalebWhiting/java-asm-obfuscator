/*
 * Copyright © 2017 Caleb Whiting <caleb.andrew.whiting@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package com.github.jasmo.obfuscate;

import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author Caleb Whiting
 */
public interface Transformer {

	void transform(Map<String, ClassNode> classMap);

}
