package com.revtek.rasmo.analyze.query;

import org.objectweb.asm.tree.*;

import java.lang.reflect.*;

/**
* @author Caleb Whiting
*/
public class NodeQuery {

	private final String[] keys;
	private final Object[] values;

	public NodeQuery(Object... values) {
		int n = values.length / 2;
		this.keys = new String[n];
		this.values = new Object[n];
		for (int i = 0; i < values.length; i += 2) {
			this.keys[i / 2] = (String) values[i];
			this.values[i / 2] = values[i + 1];
		}
	}

	public boolean test(AbstractInsnNode node) {
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			Object value = values[i];
			try {
				Field field = node.getClass().getDeclaredField(key);
				Object o = field.get(node);
				if (value == o || (o != null && value != null && value.equals(o)))
					continue;
			} catch (Exception ignored) {}
			return false;
		}
		return true;
	}


}
