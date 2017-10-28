package com.github.jasmo.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Caleb Whiting
 */
public interface UniqueStringGenerator {

	void reset();

	String next();

	/**
	 * Unpredictable and made up of characters outside of the 'normal' range
	 */
	class Crazy implements UniqueStringGenerator {

		private static final Random rng = new Random();
		private final byte[] buffer;
		private final Set<String> used = new HashSet<>();

		public Crazy(int size) {
			buffer = new byte[size];
		}

		@Override
		public void reset() {
			used.clear();
		}

		@Override
		public String next() {
			for (int i = 0; i < buffer.length; i++) {
				int low = 127; /* Skip alphabetical/numeric/descriptor special chars etc */
				buffer[i] = (byte) (low + ((byte) rng.nextInt(255-low)));
			}
			String s = new String(buffer, 0, buffer.length);
			return used.add(s) ? s : next();
		}
	}

	/**
	 * Increments similar to a number
	 * Last number that is < Z is incremented, all values after the incremented value are reset to 'A'.
	 * If all values are Z then increase length
	 */
	class Default implements UniqueStringGenerator {

		char[] chars = new char[0];

		@Override
		public void reset() {
			chars = new char[0];
		}

		@Override
		public String next() {
			for (int n = chars.length - 1; n >= 0; n--) {
				if (chars[n] < 'Z') {
					chars[n]++;
					return new String(chars);
				}
				chars[n] = 'A';
			}
			chars = new char[chars.length + 1];
			Arrays.fill(chars, 'A');
			return new String(chars);
		}

	}

}
