package gov.usgs.webservices.framework.logging;


import static org.apache.log4j.Level.WARN;
import gov.usgs.webservices.framework.utils.StringUtils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Logging class to hide dependence on choice of logging framework -- log4j or
 * java.util.logging
 * 
 * @author ilinkuo
 * 
 */
public abstract class LoggingUtils {
	public static String LOGGER_NAME="WebserviceFrameworkLogger";
	public static Level level;
	
	public static void logInfo(String baseMessage) {
		Logger.getLogger(LOGGER_NAME).info(baseMessage);
	}
	
	public static void logDebug(String baseMessage) {
		Logger.getLogger(LOGGER_NAME).debug(baseMessage);
	}
	
	public static void error(Object message, Throwable t) {
		Logger.getLogger(LOGGER_NAME).error(message, t);
	}
	
	public static void warn(String message, Throwable t) {
		Logger.getLogger(LOGGER_NAME).warn(message, t);
	}
	
	public static void warn(String... messageParts) {
		Logger logger = Logger.getLogger(LOGGER_NAME);
		if (logger.isEnabledFor(WARN)) {
			logger.warn(StringUtils.join(messageParts));
		}
	}

	public static void debug(String... messageParts) {
		Logger logger = Logger.getLogger(LOGGER_NAME);
		if (logger.isDebugEnabled()) {
			logger.debug(StringUtils.join(messageParts));
		}
	}

	public static void error(String... messageParts) {
		Logger.getLogger(LOGGER_NAME).error(StringUtils.join(messageParts));
	}

	public static void info(String... messageParts) {
		Logger logger = Logger.getLogger(LOGGER_NAME);
		if (logger.isInfoEnabled()) {
			logger.info(StringUtils.join(messageParts));
		}
	}

}
