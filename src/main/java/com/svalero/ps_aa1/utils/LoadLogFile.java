package com.svalero.ps_aa1.utils;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.svalero.ps_aa1.constants.Constants.*;

public class LoadLogFile {

    public static void show(TextArea historyArea) {
        String logPath = System.getProperty("user.home") + "\\" + MAIN_DIRECTORY + "\\" + LOGS_DIRECTORY + "\\" + FILE_LOG;
        File logFile = new File(logPath);

        if (logFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(logFile.toURI())));
                historyArea.clear();
                historyArea.appendText(content);
            } catch (IOException e) {
                HistoryLogger.logError(e.getMessage());
            }
        }
    }
}
