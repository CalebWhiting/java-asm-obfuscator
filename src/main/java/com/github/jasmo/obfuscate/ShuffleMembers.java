package com.github.jasmo.obfuscate;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.Map;

public class ShuffleMembers implements Processor  {

	private static final Logger log = LogManager.getLogger(ShuffleMembers.class);

	@Override
	public void process(Map<String, ClassNode> classMap) {
		classMap.values().forEach(c -> {
			Collections.shuffle(c.fields);
			Collections.shuffle(c.methods);
			Collections.shuffle(c.innerClasses);
			Collections.shuffle(c.interfaces);
			Collections.shuffle(c.attrs);
			Collections.shuffle(c.invisibleAnnotations);
			Collections.shuffle(c.visibleAnnotations);
			Collections.shuffle(c.invisibleTypeAnnotations);
			Collections.shuffle(c.visibleTypeAnnotations);
			c.fields.forEach(f -> {
				Collections.shuffle(f.attrs);
				Collections.shuffle(f.invisibleAnnotations);
				Collections.shuffle(f.visibleAnnotations);
				Collections.shuffle(f.invisibleTypeAnnotations);
				Collections.shuffle(f.visibleTypeAnnotations);
			});
			c.methods.forEach(m -> {
				Collections.shuffle(m.attrs);
				Collections.shuffle(m.invisibleAnnotations);
				Collections.shuffle(m.visibleAnnotations);
				Collections.shuffle(m.invisibleTypeAnnotations);
				Collections.shuffle(m.visibleTypeAnnotations);
				Collections.shuffle(m.exceptions);
				Collections.shuffle(m.invisibleLocalVariableAnnotations);
				Collections.shuffle(m.visibleLocalVariableAnnotations);
				Collections.shuffle(m.localVariables);
				Collections.shuffle(m.parameters);
			});
		});
	}

}
