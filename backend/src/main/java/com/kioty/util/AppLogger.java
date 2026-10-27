package com.kioty.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe application logger that writes formatted log records to both
 * standard output and the logs/app.log file.
 */
public class AppLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Object LOCK = new Object();
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "logs/app.log";

    static {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void info(String message) {
        log("INFO", message, null);
    }

    public static void warning(String message) {
        log("WARNING", message, null);
    }

    public static void error(String message) {
        log("ERROR", message, null);
    }

    public static void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }

    private static void log(String level, String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logLine = String.format("%s [%s] %s", timestamp, level, message);

        synchronized (LOCK) {
            if ("ERROR".equals(level)) {
                System.err.println(logLine);
                if (throwable != null) {
                    throwable.printStackTrace(System.err);
                }
            } else {
                System.out.println(logLine);
            }

            try {
                File dir = new File(LOG_DIR);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                try (FileWriter fw = new FileWriter(LOG_FILE, true);
                     PrintWriter pw = new PrintWriter(fw)) {
                    pw.println(logLine);
                    if (throwable != null) {
                        throwable.printStackTrace(pw);
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
        }
    }
}
