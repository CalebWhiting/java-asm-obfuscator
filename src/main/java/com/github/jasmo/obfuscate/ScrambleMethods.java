package com.github.jasmo.obfuscate;

import com.github.jasmo.util.JRE;
import com.github.jasmo.util.UniqueString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.*;

/**
 * @author Caleb Whiting
 *         <p>
 *         Obfuscates the names of methods in the given library
 */
public class ScrambleMethods implements Processor {

	private static final Logger log = LogManager.getLogger(ScrambleMethods.class);

	private JRE env;
	private Map<String, ClassNode> classMap;

	@Override
	public void process(Map<String, ClassNode> classMap) {
		this.classMap = classMap;
		// loads all jre libraries from 'java.class.path' system property
		this.env = JRE.getJRE();
		// todo: add more in-depth verification
		List<String> pass = Arrays.asList("main", "<init>", "<clinit>", "createUI");
		// reset the unique string generator, so that is starts at 'a'
		UniqueString.reset();
		Map<String, String> mappings = new HashMap<>();
		List<MethodNode> methods = new LinkedList<>();
		for (ClassNode c : classMap.values())
			methods.addAll(c.methods);
		// shuffle the methods so that there isn't a naming pattern
		Collections.shuffle(methods);
		// create obfuscated name mappings
		methods:
		for (MethodNode m : methods) {
			ClassNode owner = getOwner(m);
			// skip entry points, constructors etc
			if (pass.contains(m.name)) {
				log.debug("Skipping method: {}.{}{}", owner.name, m.name, m.desc);
				continue;
			}
			Stack<ClassNode> stack = new Stack<>();
			stack.add(owner);
			// check this is the top-level method
			while (stack.size() > 0) {
				ClassNode node = stack.pop();
				if (node != owner && getMethod(node, m.name, m.desc) != null)
					// not top-level member
					continue methods;
				// push superclass
				ClassNode parent = getClassNode(node.superName);
				if (parent != null)
					stack.push(parent);
				// push interfaces
				Set<ClassNode> interfaces = new HashSet<>();
				String[] interfacesNames = node.interfaces.toArray(new String[node.interfaces.size()]);
				for (int i = 0; i < interfacesNames.length; i++) {
					ClassNode iface = getClassNode(interfacesNames[i]);
					if (iface != null) {
						interfaces.add(iface);
					}
				}
				stack.addAll(interfaces);
			}
			// generate obfuscated name
			String name = UniqueString.next();
			log.debug("Mapping method {}.{}{} to {}.{}{}", owner.name, m.name, m.desc, owner.name, name, m.desc);
			stack.add(owner);
			// go through all sub-classes, and define the new name
			// regardless of if the method exists in the given class or not
			while (stack.size() > 0) {
				ClassNode node = stack.pop();
				String key = node.name + '.' + m.name + m.desc;
				mappings.put(key, name);
				// push subclasses
				classMap.values().forEach(c -> {
					if (c.superName.equals(node.name) || c.interfaces.contains(node.name))
						stack.push(c);
				});
			}
		}
		// apply transformation
		SimpleRemapper remapper = new SimpleRemapper(mappings);
		for (ClassNode node : new ArrayList<>(classMap.values())) {
			ClassNode clone = new ClassNode();
			ClassRemapper adapter = new ClassRemapper(clone, remapper);
			node.accept(adapter);
			classMap.put(node.name, clone);
		}
	}

	private MethodNode getMethod(ClassNode node, String name, String desc) {
		return findFirst(node.methods, m -> m.name.equals(name) && m.desc.equals(desc));
	}

	private ClassNode getClassNode(String name) {
		if (name == null) return null;
		ClassNode n = classMap.get(name);
		return n == null ? env.getClassMap().get(name) : n;
	}

	private ClassNode getOwner(MethodNode m) {
		return findFirst(classMap.values(), c -> c.methods.contains(m));
	}

	private <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
		for (T t : collection)
			if (predicate.test(t))
				return t;
		return null;
	}

}
