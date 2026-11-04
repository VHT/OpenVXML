package org.eclipse.vtp.framework.util;

/**
 * Replacement for the OSGi <code>LogService</code>. ERROR and WARNING entries
 * are written to <code>System.err</code>; INFO and DEBUG entries are
 * discarded.
 */
public class Log {
	/** An error message (same value as <code>LogService.LOG_ERROR</code>). */
	public static final int LOG_ERROR = 1;
	/** A warning message (same value as <code>LogService.LOG_WARNING</code>). */
	public static final int LOG_WARNING = 2;
	/** An informational message (same value as <code>LogService.LOG_INFO</code>). */
	public static final int LOG_INFO = 3;
	/** A debug message (same value as <code>LogService.LOG_DEBUG</code>). */
	public static final int LOG_DEBUG = 4;

	/**
	 * Logs a message.
	 *
	 * @param level
	 *            The severity of the message.
	 * @param message
	 *            The message to log.
	 */
	public void log(int level, String message) {
		log(level, message, null);
	}

	/**
	 * Logs a message with an exception.
	 *
	 * @param level
	 *            The severity of the message.
	 * @param message
	 *            The message to log.
	 * @param thrown
	 *            The exception associated with the message, or
	 *            <code>null</code>.
	 */
	public void log(int level, String message, Throwable thrown) {
		if (level != LOG_ERROR && level != LOG_WARNING) {
			return;
		}
		System.err.println(message);
		if (thrown != null) {
			thrown.printStackTrace();
		}
	}
}
