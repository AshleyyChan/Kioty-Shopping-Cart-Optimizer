package com.kioty.service;

import com.kioty.model.OptimizeResult;
import com.kioty.util.AppLogger;
import com.kioty.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service to manage optimization session persistence and historical logs.
 */
public class HistoryService {
    private static final String LOGS_DIR = "logs";
    private static final String HISTORY_FILE = "logs/history.json";
    private static final Object FILE_LOCK = new Object();

    public static String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * Saves optimization session to daily log directory and appends to history.json.
     */
    public static void saveSessionResult(String sessionId, OptimizeResult result) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        File dailyDir = new File(LOGS_DIR, dateStr);
        if (!dailyDir.exists()) {
            dailyDir.mkdirs();
        }

        File sessionFile = new File(dailyDir, "session_" + sessionId + ".json");
        String prettyJson = JsonUtil.toPrettyJson(result);

        synchronized (FILE_LOCK) {
            try {
                Files.writeString(sessionFile.toPath(), prettyJson, StandardCharsets.UTF_8);
                AppLogger.info("Session " + sessionId + " saved to " + sessionFile.getPath());
            } catch (IOException e) {
                AppLogger.error("Failed to write session file: " + sessionFile.getPath(), e);
            }

            appendToHistory(result, "guest_user");
        }
    }

    /**
     * Appends session result to history.json under the given user ID.
     */
    @SuppressWarnings("unchecked")
    public static void appendToHistory(OptimizeResult result, String userId) {
        synchronized (FILE_LOCK) {
            File logsDir = new File(LOGS_DIR);
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            File histFile = new File(HISTORY_FILE);
            Map<String, Object> historyMap = new LinkedHashMap<>();

            if (histFile.exists()) {
                try {
                    String content = Files.readString(histFile.toPath(), StandardCharsets.UTF_8);
                    Object parsed = JsonUtil.parse(content);
                    if (parsed instanceof Map) {
                        historyMap = (Map<String, Object>) parsed;
                    }
                } catch (Exception e) {
                    AppLogger.warning("Could not read existing history.json, reinitializing: " + e.getMessage());
                }
            }

            Object userEntriesObj = historyMap.get(userId);
            List<Object> userEntries;
            if (userEntriesObj instanceof List) {
                userEntries = (List<Object>) userEntriesObj;
            } else {
                userEntries = new ArrayList<>();
                historyMap.put(userId, userEntries);
            }

            // Convert result to map for JSON serialization
            userEntries.add(result);

            try {
                String updatedJson = JsonUtil.toPrettyJson(historyMap);
                Files.writeString(histFile.toPath(), updatedJson, StandardCharsets.UTF_8);
            } catch (IOException e) {
                AppLogger.error("Failed to append to history.json", e);
            }
        }
    }

    /**
     * Retrieves optimization history for a user.
     */
    @SuppressWarnings("unchecked")
    public static List<Object> getHistory(String userId) {
        synchronized (FILE_LOCK) {
            File histFile = new File(HISTORY_FILE);
            if (!histFile.exists()) {
                return Collections.emptyList();
            }

            try {
                String content = Files.readString(histFile.toPath(), StandardCharsets.UTF_8);
                Object parsed = JsonUtil.parse(content);
                if (parsed instanceof Map) {
                    Map<String, Object> historyMap = (Map<String, Object>) parsed;
                    Object userEntries = historyMap.get(userId);
                    if (userEntries instanceof List) {
                        return (List<Object>) userEntries;
                    }
                }
            } catch (Exception e) {
                AppLogger.error("Failed to read history from " + HISTORY_FILE, e);
            }
            return Collections.emptyList();
        }
    }
}
