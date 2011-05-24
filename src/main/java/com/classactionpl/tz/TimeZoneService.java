/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package com.classactionpl.tz;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * Our main service interface for providing time zone services.
 * 
 * @author huntc
 * 
 */
public class TimeZoneService {

	/** */
	private static final int MILLIS_PER_SECOND = 1000;

	/**
	 * Return a list of available TZ ids.
	 * 
	 * @return the list.
	 */
	public List<String> getAvailableIDs() {
		return Arrays.asList(ZoneinfoTimeZone.getAvailableIDs());
	}

	/**
	 * Return the local time offset for a given time zone id and utc relative
	 * offset.
	 * 
	 * @param id
	 *            the time zone identifier e.g. "Australia/Sydney"
	 * @param when
	 *            the utc related time.
	 * @return the utc offset for the time represented by when. Offsets are
	 *         expressed in milliseconds.
	 */
	public Integer getOffset(String id, Date when) {
		Integer offset;

		ZoneinfoTimeZone tz = (ZoneinfoTimeZone) ZoneinfoTimeZone
				.getTimeZone(id);

		if (tz != null) {
			AbstractZone zone = tz.getZone();

			ZoneDetail zoneDetail = zone.resolveDetail(when);
			assert (zoneDetail != null);

			Rule rule = zoneDetail.resolveRule(when);
			if (rule != null) {
				offset = (zoneDetail.getUtcOffset() + rule.getSave())
						* MILLIS_PER_SECOND;
			} else {
				offset = zoneDetail.getUtcOffset() * MILLIS_PER_SECOND;
			}
		} else {
			throw new TimeZoneNotFound(id);
		}

		return offset;
	}

	/**
	 * Refresh the time zones given a file passed in.
	 * 
	 * @param zoneInfoFile
	 *            the file to be parsed.
	 * @throws Exception
	 *             if something goes wrong.
	 */
	public void refreshTimeZones(File zoneInfoFile) throws Exception {
		if (zoneInfoFile.isFile() && zoneInfoFile.getName().endsWith(".tar.gz")) {
			BufferedInputStream is = new BufferedInputStream(
					new FileInputStream(zoneInfoFile));
			CompressorInputStream gzippedIs = new CompressorStreamFactory()
					.createCompressorInputStream(CompressorStreamFactory.GZIP,
							is);
			TarArchiveInputStream tarIs = new TarArchiveInputStream(gzippedIs);

			try {
				// FIXME: Load time zones.
			} finally {
				tarIs.close();
			}
			zoneInfoFile.delete();
		}
	}
}
