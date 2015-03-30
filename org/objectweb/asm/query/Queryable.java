package org.objectweb.asm.query;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Caleb Whiting
 *         <p>
 *         Retrives the information corresponding to the given key.
 *         This is an alternative to reflecting the values, it is faster.
 */
public interface Queryable {

	public default Object query(String key) {
		return null;
	}

	public default boolean check(Object... values) {
		for (int i = 0; i < values.length; i += 2) {
			String key = (String) values[i];
			Object value = values[i + 1];
			Object o = query(key);
			if (!QueryUtil.isEqual(o, value))
				return false;
		}
		return true;
	}

	public default boolean check(Query query) {
		return check(query.values());
	}

}
