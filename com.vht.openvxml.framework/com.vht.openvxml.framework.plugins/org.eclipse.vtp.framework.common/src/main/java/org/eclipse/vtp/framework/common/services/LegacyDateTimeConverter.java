/**
 * 
 */
package org.eclipse.vtp.framework.common.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Sandeep Velagaleti
 *
 */
public class LegacyDateTimeConverter {

	public static LocalDateTime toLocalDateTime(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		TimeZone tz = calendar.getTimeZone();
		ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
		return LocalDateTime.ofInstant(calendar.toInstant(), zid);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * LocalDateTime ldt = LocalDateTime.now();
		 * System.out.println(Calendar.getInstance());
		 * System.out.println(TimeObjectConverter.toCalendar(ldt));
		 */

	}

}
