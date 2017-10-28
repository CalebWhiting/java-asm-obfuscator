package com.github.jasmo.obfuscate;

import com.github.jasmo.util.BytecodeHelper;
import com.github.jasmo.util.UniqueStringGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * @author Caleb Whiting
 */
public class ScrambleClasses implements Processor {

	private static final Logger log = LogManager.getLogger(ScrambleClasses.class);

	private UniqueStringGenerator generator;
	private final String basePackage;
	private final List<String> skip;

	public ScrambleClasses(UniqueStringGenerator generator, String basePackage, String... skip) {
		this.generator = generator;
		this.basePackage = basePackage.replace('.', '/');
		for (int i = 0; i < skip.length; i++) {
			skip[i] = skip[i].replace('.', '/');
		}
		this.skip = Arrays.asList(skip);
	}

	@Override
	public void process(Map<String, ClassNode> classMap) {
		generator.reset();
		Map<String, String> remap = new HashMap<>();
		List<String> keys = new ArrayList<>(classMap.keySet());
		// shuffle order in which names are assigned
		// so that they're not always assigned the same name
		Collections.shuffle(keys);
		for (String key : keys) {
			ClassNode cn = classMap.get(key);
			String name = cn.name;
			if (!skip.contains(name)) {
				name = generator.next();
				name = basePackage + "/" + name;
			}
			remap.put(cn.name, name);
			log.debug("Mapping class {} to {}", cn.name, name);
		}
		BytecodeHelper.applyMappings(classMap, remap);
	}

}
