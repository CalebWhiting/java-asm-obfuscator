package demo;

import com.revtek.rasmo.obfuscate.*;

import java.io.*;
import java.util.jar.*;

/**
 * @author Caleb Whiting
 */
public class Main {

	public static void main(String[] args) {
		try {
			Obfuscator obfuscator = new Obfuscator();
			obfuscator.supply(new JarFile("./test.jar"));
			obfuscator.apply(new ScrambleClasses("com.runebox", "com.runebox.Launch"));
			obfuscator.apply(new ScrambleFields());
			obfuscator.apply(new ScrambleMethods());
			obfuscator.apply(new InlineFields());
			try (JarOutputStream out = new JarOutputStream(new FileOutputStream("./test_.jar"))) {
				obfuscator.write(out);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
