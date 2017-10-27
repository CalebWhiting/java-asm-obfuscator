package com.github.jasmo.obfuscate;

import com.github.jasmo.util.UniqueString;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Caleb Whiting
 */
public class ScrambleFields implements Processor {

	@Override
	public void process(Map<String, ClassNode> classMap) {
		Map<String, String> remap = new HashMap<>();
		UniqueString.reset();
		List<FieldNode> fields = new ArrayList<>();
		for (ClassNode c : classMap.values()) {
			fields.addAll(c.fields);
		}
		Collections.shuffle(fields);
		for (FieldNode f : fields) {
			ClassNode c = getOwner(f, classMap);
			String name = UniqueString.next();
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
		SimpleRemapper remapper = new SimpleRemapper(remap);
		for (ClassNode node : new ArrayList<>(classMap.values())) {
			ClassNode copy = new ClassNode();
			ClassRemapper adapter = new ClassRemapper(copy, remapper);
			node.accept(adapter);
			classMap.put(node.name, copy);
		}
	}

	private ClassNode getOwner(FieldNode f, Map<String, ClassNode> classMap) {
		for (ClassNode c : classMap.values())
			if (c.fields.contains(f))
				return c;
		return null;
	}

}
