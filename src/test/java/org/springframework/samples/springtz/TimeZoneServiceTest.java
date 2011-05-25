package org.springframework.samples.springtz;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.compress.compressors.CompressorException;
import org.junit.Test;

import com.classactionpl.tz.ZoneinfoTimeZone;

public class TimeZoneServiceTest {

	@Test
	public void testParseZoneinfoFile() throws URISyntaxException,
			CompressorException, IOException {
		TimeZoneService tzService = new TimeZoneService();
		File zoneInfoFile = new File(this.getClass().getResource("australasia")
				.toURI());
		tzService.parseZoneinfoFile(zoneInfoFile);

		TimeZone tz = ZoneinfoTimeZone.getTimeZone("Australia/Sydney");

		int offset = tz.getOffset(GregorianCalendar.AD, 2011, Calendar.MAY, 31,
				Calendar.TUESDAY, 15 * 60 * 60 * 1000);
		assertEquals(10 * 60 * 60 * 1000, offset);
	}
}
