package com.github.jasmo.util;

import java.io.*;
import java.net.*;
import java.util.jar.*;

public class IO {

	public static final String USER_AGENT;

	public static final int BUFFER_SIZE = 512;

	static {
		StringBuilder agent = new StringBuilder();
		String[] info = {
				"Mozilla/5.0 (Windows NT 6.1; WOW64)",
				"AppleWebKit/537.36 (KHTML, like Gecko)",
				"Chrome/41.0.2272.76",
				"Safari/537.36"
		};
		for (int i = 0; i < info.length; i++) {
			if (i > 0) agent.append(" ");
			agent.append(info[i]);
		}
		USER_AGENT = agent.toString();
	}

	public static void readAndWrite(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int read;
		while ((read = in.read(buffer, 0, buffer.length)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public static void write(byte[] bytes, OutputStream out) throws IOException {
		out.write(bytes);
		out.close();
	}

	public static byte[] read(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		readAndWrite(in, out);
		byte[] bytes = out.toByteArray();
		out.close();
		return bytes;
	}

	public static int getJarHash(URL location) throws IOException {
		try (JarInputStream in = new JarInputStream(location.openStream())) {
			return in.getManifest().hashCode();
		}
	}

}
