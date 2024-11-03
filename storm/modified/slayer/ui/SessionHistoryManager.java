package net.runelite.client.plugins.microbot.storm.modified.slayer.ui;

import net.runelite.client.plugins.microbot.storm.modified.slayer.ui.SessionData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SessionHistoryManager {

    private static final String FILE_PATH = "session_history.ser"; // File path for saving session history

    // Save session data to file
    public static void saveSessionHistory(List<SessionData> sessionDataList) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(sessionDataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load session data from file
    public static List<SessionData> loadSessionHistory() {
        List<SessionData> sessionDataList = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                sessionDataList = (List<SessionData>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return sessionDataList;
    }
}
