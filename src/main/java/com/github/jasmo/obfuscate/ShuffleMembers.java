/*
 * Copyright © 2017 Caleb Whiting <caleb.andrew.whiting@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package com.github.jasmo.obfuscate;

import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ShuffleMembers implements Transformer {

	@Override
	public void transform(Map<String, ClassNode> classMap) {
		classMap.values().forEach(c -> {
			shuffle(c.fields);
			shuffle(c.methods);
			shuffle(c.innerClasses);
			shuffle(c.interfaces);
			shuffle(c.attrs);
			shuffle(c.invisibleAnnotations);
			shuffle(c.visibleAnnotations);
			shuffle(c.invisibleTypeAnnotations);
			shuffle(c.visibleTypeAnnotations);
			c.fields.forEach(f -> {
				shuffle(f.attrs);
				shuffle(f.invisibleAnnotations);
				shuffle(f.visibleAnnotations);
				shuffle(f.invisibleTypeAnnotations);
				shuffle(f.visibleTypeAnnotations);
			});
			c.methods.forEach(m -> {
				shuffle(m.attrs);
				shuffle(m.invisibleAnnotations);
				shuffle(m.visibleAnnotations);
				shuffle(m.invisibleTypeAnnotations);
				shuffle(m.visibleTypeAnnotations);
				shuffle(m.exceptions);
				shuffle(m.invisibleLocalVariableAnnotations);
				shuffle(m.visibleLocalVariableAnnotations);
				shuffle(m.localVariables);
				shuffle(m.parameters);
			});
			c.innerClasses.clear();
		});
	}

	private void shuffle(List<?> list) {
		if (list!=null)Collections.shuffle(list);
	}

}
