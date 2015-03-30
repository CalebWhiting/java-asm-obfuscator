package com.revtek.rasmo.obfuscate;

import com.revtek.rasmo.util.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author Caleb Whiting
 */
public class ScrambleClasses implements Processor {

	private final String basePackage;
	private final List<String> skip;

	public ScrambleClasses(String basePackage, String... skip) {
		this.basePackage = basePackage.replace('.', '/');
		for (int i = 0; i < skip.length; i++) {
			skip[i] = skip[i].replace('.', '/');
		}
		this.skip = Arrays.asList(skip);
	}

	@Override
	public void process(Map<String, ClassNode> classMap) {
		UniqueString.reset();
		Map<String, String> remap = new HashMap<>();
		List<String> keys = new ArrayList<>(classMap.keySet());
		// shuffle order in which names are assigned
		// so that they're not always assigned the same name
		Collections.shuffle(keys);
		for (String key : keys) {
			ClassNode cn = classMap.get(key);
			String name = cn.name;
			if (!skip.contains(name)) {
				name = UniqueString.next();
				name = basePackage + "/" + name;
			}
			remap.put(cn.name, name);
		}
		SimpleRemapper remapper = new SimpleRemapper(remap);
		for (ClassNode node : new ArrayList<>(classMap.values())) {
			ClassNode copy = new ClassNode();
			RemappingClassAdapter adapter = new RemappingClassAdapter(copy, remapper);
			node.accept(adapter);
			classMap.put(node.name, copy);
		}
		for (Object o : remap.entrySet()) {
			System.out.println("Class Remapping: " + o);
		}
	}

}
