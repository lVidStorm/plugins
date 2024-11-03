package net.runelite.client.plugins.microbot.storm.modified.slayer.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;

public class SessionManager {
    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());
    private static final String FILE_PATH = "session_data.json";

    public static void saveSessions(List<SessionData> sessions) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(sessions, writer);
        } catch (IOException e) {
            logger.severe("Error saving session data: " + e.getMessage());
        }
    }

    public static List<SessionData> loadSessions() {
        Gson gson = new Gson();
        List<SessionData> sessions = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        if (Files.exists(path)) {
            try {
                String json = new String(Files.readAllBytes(path));
                Type sessionListType = new TypeToken<ArrayList<SessionData>>() {}.getType();
                sessions = gson.fromJson(json, sessionListType);
            } catch (IOException e) {
                logger.severe("Error loading session data: " + e.getMessage());
            }
        } else {
            logger.info("Session file does not exist. Returning empty session list.");
        }
        return sessions;
    }
}
