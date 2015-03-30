package org.objectweb.asm.query;

/**
 * @author Caleb Whiting
 */
public class Query {

	private final Object[] values;

	public Query(Object... values) {
		this.values = values;
	}

	public Object[] values() {
		return this.values;
	}

}
