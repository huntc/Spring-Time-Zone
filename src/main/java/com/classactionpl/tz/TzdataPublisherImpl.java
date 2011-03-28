package com.classactionpl.tz;

import org.springframework.integration.annotation.Payload;
import org.springframework.integration.annotation.Publisher;

/**
 * Spring Integration implementation for publishing tzdata.
 * 
 * @author huntc
 * 
 */
public class TzdataPublisherImpl implements TzdataPublisher {

	@Override
	@Publisher(channel = "publishTzdata")
	@Payload("#args.tzdata")
	public void publish(String tzdata) {
	}

}
