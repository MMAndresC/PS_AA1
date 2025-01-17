package com.svalero.ps_aa1.utils;

import java.io.IOException;
import java.util.logging.*;

import static com.svalero.ps_aa1.constants.Constants.*;

public class HistoryLogger {
    private static final Logger logger = Logger.getLogger(HistoryLogger.class.getName());
    private static final Logger errorLogger = Logger.getLogger("ErrorLogger");

    static {
        try{
            String path = System.getProperty("user.home") + "\\" + MAIN_DIRECTORY + "\\" + LOGS_DIRECTORY;
            FileHandler historyHandler = new FileHandler(path + "\\" + FILE_LOG, true);
            historyHandler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord record) {

                    return String.format("%1$tF %1$tT - %2$s %n",
                            record.getMillis(),
                            record.getMessage());
                }
            });
            FileHandler errorHandler = new FileHandler(path + "\\" + FILE_ERROR_LOG, true);
            errorHandler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord record) {
                    return String.format("%1$tF %1$tT - %2$s%n", record.getMillis(), record.getMessage());
                }
            });
            logger.addHandler(historyHandler);
            errorLogger.addHandler(errorHandler);
            //no show in console
            logger.setUseParentHandlers(false);
            errorLogger.setUseParentHandlers(false);
        }catch(IOException e){
            System.err.println("Error al configurar el logger: " + e.getMessage());
        }
    }
    public static void log(String message) {
        logger.log(Level.INFO, message);
    }

    public static void logError(String error) {
        errorLogger.log(Level.SEVERE, error);
    }

}
