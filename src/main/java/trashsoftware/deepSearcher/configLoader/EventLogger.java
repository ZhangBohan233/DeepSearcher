package trashsoftware.deepSearcher.configLoader;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class EventLogger {


    /**
     * Record an Exception information to log file (trace.log).
     *
     * @param stackTrace the Throwable object which cause this logging action.
     * @param message the error message.
     * @param level the logging level.
     */
    public static void log(Throwable stackTrace, String message, Level level) {
        try {
            String logFileName = "trace.log";
            Logger logger = Logger.getLogger(logFileName);
            FileHandler handler = new FileHandler(logFileName);
            logger.addHandler(handler);

            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);

            logger.log(level, message, stackTrace);
            handler.close();
        } catch (IOException ioe) {
            //
        }
    }
}
