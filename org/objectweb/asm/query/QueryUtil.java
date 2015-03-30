package org.objectweb.asm.query;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author Caleb Whiting
 */
public class QueryUtil {

	public static boolean isEqual(Object o, Object value) {
		if (value != null && value instanceof AnyOf) {
			for (Object ob : ((AnyOf) value).values()) {
				if (isEqual(o, ob))
					return true;
			}
			return false;
		}
		if ((o == null && value != null) || (value == null && o != null)) {
			return false;
		}
		if (o == value) {
			return true;
		}
		if (value.getClass().isArray() && o.getClass().isArray()) {
			int n = Array.getLength(value);
			if (n != Array.getLength(o))
				return false;
			Object[] array1 = new Object[n];
			Object[] array2 = new Object[n];
			for (int j = 0; j < n; j++) {
				array1[j] = Array.get(value, j);
				array2[j] = Array.get(o, j);
			}
			return Arrays.equals(array1, array2);
		}
		return value.equals(o);
	}

}
