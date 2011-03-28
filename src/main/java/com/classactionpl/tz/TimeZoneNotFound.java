package com.classactionpl.tz;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a time zone object cannot be found.
 * 
 * @author huntc
 * 
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TimeZoneNotFound extends RuntimeException {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct with tz id.
	 * 
	 * @param id
	 *            the id of the time zone.
	 */
	public TimeZoneNotFound(String id) {
		super("TZ not found: " + id);
	}
}
