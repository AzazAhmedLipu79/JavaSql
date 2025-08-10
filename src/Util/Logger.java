package Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	public enum Level { DEBUG, INFO, WARN, ERROR }

	private static Level fileLevel = Level.INFO;   // what goes to file
	private static Level consoleLevel = Level.WARN; // what goes to console (default quieter)
	private static PrintWriter writer;
	private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	static {
		try {
			Path outDir = Paths.get("out");
			if (!Files.exists(outDir)) Files.createDirectories(outDir);
			writer = new PrintWriter(Files.newBufferedWriter(outDir.resolve("dbms.log")));
		} catch (IOException e) {
			writer = null;
		}
	}

	public static void setFileLevel(Level lvl) { fileLevel = lvl; }
	public static void setConsoleLevel(Level lvl) { consoleLevel = lvl; }

	private static void log(Level lvl, String msg) {
		String line = String.format("%s [%s] %s", LocalDateTime.now().format(TS), lvl, msg);
		// console
		if (lvl.ordinal() >= consoleLevel.ordinal()) {
			System.out.println(line);
		}
		// file
		if (writer != null && lvl.ordinal() >= fileLevel.ordinal()) {
			writer.println(line);
			writer.flush();
		}
	}

	public static void debug(String msg) { log(Level.DEBUG, msg); }
	public static void info(String msg) { log(Level.INFO, msg); }
	public static void warn(String msg) { log(Level.WARN, msg); }
	public static void error(String msg) { log(Level.ERROR, msg); }
}
