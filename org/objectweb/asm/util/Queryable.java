package org.objectweb.asm.util;

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
			if (value != null && value.getClass().isArray()) {
				if (o == null || !o.getClass().isArray())
					return false;
				int n = Array.getLength(value);
				if (n != Array.getLength(o))
					return false;
				Object[] array1 = new Object[n];
				Object[] array2 = new Object[n];
				for (int j = 0; j < n; j++) {
					array1[j] = Array.get(value, j);
					array2[j] = Array.get(o, j);
				}
				if (Arrays.equals(array1, array2))
					continue;
				return false;
			} else {
				if ((o == null && value != null) || (value == null && o != null)) {
					return false;
				}
				if (value == o || value.equals(o)) {
					continue;
				}
				return false;
			}
		}
		return true;
	}

}
