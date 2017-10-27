package com.github.jasmo.util;

/**
 * @author Caleb Whiting
 */
public class UniqueString {

	private static final String FIRST;

	static {
		FIRST = "a";
	}

	private static String text = "";

	public static void reset() {
		text = FIRST;
	}

	public static String next() {
		int len = text.length();
		if (len == 0)
			return text = FIRST;
		boolean alphaNum = false;
		int alphaNumPos = -1;
		for (char c : text.toCharArray()) {
			alphaNumPos++;
			if (Character.isDigit(c) || Character.isLetter(c)) {
				alphaNum = true;
				break;
			}
		}
		StringBuilder buf = new StringBuilder(text);
		if (!alphaNum || alphaNumPos == 0 || alphaNumPos == len) {
			next(buf, buf.length() - 1, alphaNum);
		} else {
			String prefix = text.substring(0, alphaNumPos);
			buf = new StringBuilder(text.substring(alphaNumPos));
			next(buf, buf.length() - 1, true);
			buf.insert(0, prefix);
		}
		return text = buf.toString();
	}

	private static void next(StringBuilder buf, int pos, boolean alphaNum) {
		if (pos == -1) {
			char c = buf.charAt(0);
			String rep;
			if (Character.isDigit(c))
				rep = "1";
			else if (Character.isLowerCase(c))
				rep = "a";
			else if (Character.isUpperCase(c))
				rep = "A";
			else
				rep = Character.toString((char) (c + 1));
			buf.insert(0, rep);
			return;
		}
		char c = buf.charAt(pos);
		if (Character.isDigit(c)) {
			if (c == '9') {
				buf.replace(pos, pos + 1, "0");
				next(buf, pos - 1, alphaNum);
			} else {
				buf.replace(pos, pos + 1, Character.toString((char) (c + 1)));
			}
		} else if (Character.isLowerCase(c)) {
			if (c == 'z') {
				buf.replace(pos, pos + 1, "a");
				next(buf, pos - 1, alphaNum);
			} else {
				buf.replace(pos, pos + 1, Character.toString((char) (c + 1)));
			}
		} else if (Character.isUpperCase(c)) {
			if (c == 'Z') {
				buf.replace(pos, pos + 1, "A");
				next(buf, pos - 1, alphaNum);
			} else {
				buf.replace(pos, pos + 1, Character.toString((char) (c + 1)));
			}
		} else {
			if (alphaNum) {
				next(buf, pos - 1, true);
			} else {
				if (c == Character.MAX_VALUE) {
					buf.replace(pos, pos + 1, Character.toString(Character.MIN_VALUE));
					next(buf, pos - 1, false);
				} else {
					buf.replace(pos, pos + 1, Character.toString((char) (c + 1)));
				}
			}
		}
	}
}
