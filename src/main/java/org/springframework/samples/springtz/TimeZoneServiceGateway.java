package org.springframework.samples.springtz;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;

/**
 * Messaging gateway interface into our messaging system.
 */
public interface TimeZoneServiceGateway {
	Future<List<String>> getAvailableIDs(@Payload Object nullParam);

	Integer getOffset(@Payload String id, @Header("when") Date when);
}
