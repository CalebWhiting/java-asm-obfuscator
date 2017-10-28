package com.github.jasmo;

import com.github.jasmo.obfuscate.*;
import com.github.jasmo.util.UniqueStringGenerator;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.nio.file.Paths;

public class Bootstrap {

	private static final Logger log = LogManager.getLogger(Bootstrap.class);

	public static void main(String[] args) {
		Options options = new Options()
				                  .addOption("h", "help", false, "Print help message")
				                  .addOption("v", "verbose", false, "Increase verbosity")
				                  .addOption("c", "cfn", true, "Enable 'crazy fucking names and set name length (large names == large output size)'")
				                  .addOption("p", "package", true, "Move obfuscated classes to this package")
				                  .addOption("k", "keep", true, "Don't rename this class")
				                  .addOption("v", "verbose", false, "Increase verbosity");
		try {
			CommandLineParser clp = new DefaultParser();
			CommandLine cl = clp.parse(options, args);
			if (cl.hasOption("help")) {
				help(options);
				return;
			}
			if (cl.hasOption("verbose")) {
				LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
				Configuration config = ctx.getConfiguration();
				LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
				loggerConfig.setLevel(Level.DEBUG);
				ctx.updateLoggers();
			}
			String[] keep = cl.getOptionValues("keep");
			if (cl.getArgList().size() < 2) {
				throw new ParseException("Expected at-least two arguments");
			}
			log.debug("Input: {}, Output: {}", cl.getArgList().get(0), cl.getArgList().get(1));
			Obfuscator o = new Obfuscator();
			try {
				o.supply(Paths.get(cl.getArgList().get(0)));
			} catch (Exception e) {
				log.error("An error occurred while reading the source target", e);
				return;
			}
			try {
				UniqueStringGenerator usg;
				if (cl.hasOption("cfn")) {
					int size = Integer.parseInt(cl.getOptionValue("cfn"));
					usg = new UniqueStringGenerator.Crazy(size);
				} else {
					usg = new UniqueStringGenerator.Default();
				}
				o.apply(new FullAccessFlags());
				o.apply(new ScrambleStrings());
				o.apply(new ScrambleClasses(usg, cl.getOptionValue("package", ""), keep == null ? new String[0] : keep));
				o.apply(new ScrambleFields(usg));
				o.apply(new ScrambleMethods(usg));
				o.apply(new InlineAccessors());
				o.apply(new RemoveDebugInfo());
				o.apply(new ShuffleMembers());
			} catch (Exception e) {
				log.error("An error occurred while applying transform", e);
				return;
			}
			try {
				o.write(Paths.get(cl.getArgList().get(1)));
			} catch (Exception e) {
				log.error("An error occurred while writing to the destination target", e);
				return;
			}
		} catch (ParseException e) {
			log.error("Failed to parse command line arguments", e);
			help(options);
		}
	}

	private static void help(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar jasmo.jar <src> <dest>", options, true);
	}

}
