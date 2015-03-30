package org.objectweb.asm.query;

/**
 * @author Caleb Whiting
 */
public class AnyOf {

	private final Object[] values;

	public AnyOf(Object... values) {
		this.values = values;
	}

	public Object[] values() {
		return values;
	}

}
