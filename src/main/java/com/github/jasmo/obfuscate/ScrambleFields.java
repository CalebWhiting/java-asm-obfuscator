package com.github.jasmo.obfuscate;

import com.github.jasmo.util.BytecodeHelper;
import com.github.jasmo.util.UniqueStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Caleb Whiting
 */
public class ScrambleFields implements Transformer {
	private static final Logger log = LogManager.getLogger("ScrambleFields");
	private final UniqueStringGenerator generator;

	public ScrambleFields(UniqueStringGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void transform(Map<String, ClassNode> classMap) {
		Map<String, String> remap = new HashMap<>();
		generator.reset();
		List<FieldNode> fields = new ArrayList<>();
		for (ClassNode c : classMap.values()) fields.addAll(c.fields);
		Collections.shuffle(fields);
		for (FieldNode f : fields) {
			ClassNode c = getOwner(f, classMap);
			String name = generator.next();
			Stack<ClassNode> stack = new Stack<>();
			stack.add(c);
			while (stack.size() > 0) {
				ClassNode node = stack.pop();
				String key = node.name + "." + f.name;
				remap.put(key, name);
				stack.addAll(classMap.values().stream().
						filter(cn -> cn.superName.equals(node.name)).
						                     collect(Collectors.toList()));
			}
		}
		BytecodeHelper.applyMappings(classMap, remap);
	}

	private ClassNode getOwner(FieldNode f, Map<String, ClassNode> classMap) {
		for (ClassNode c : classMap.values())
			if (c.fields.contains(f))
				return c;
		return null;
	}

}
