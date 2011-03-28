package com.classactionpl.tz;


/**
 * Responsible for publishing time zone data.
 * 
 */
public interface TzdataPublisher {
	/**
	 * Perform the publishing.
	 * 
	 * @param data
	 *            the data to publish.
	 */
	void publish(String data);
}
