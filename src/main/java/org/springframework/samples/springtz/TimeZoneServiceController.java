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

package org.springframework.samples.springtz;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles requests for the timezone service.
 */
@Controller
@RequestMapping("/tz")
public class TimeZoneServiceController {

	private TimeZoneServiceGateway timeZoneServiceGateway;

	/**
	 * Get the available ids.
	 * 
	 * @return a list of ids.
	 * @throws IOException
	 *             something went wrong.
	 */
	@RequestMapping(value = "ids", method = RequestMethod.GET)
	@ResponseBody
	public Future<List<String>> getAvailableIDs(
			final HttpServletResponse httpResponse) throws IOException {

		final Future<List<String>> futureResult = timeZoneServiceGateway
				.getAvailableIDs("");

		return new FutureTask<List<String>>(new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				List<String> result = futureResult.get();

				// Pass back a 404 if we've got nothing to return.
				if (result == null) {
					httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
				return result;
			}

		});
	}

	/**
	 * Get the offsets.
	 * 
	 * @param country
	 *            the country.
	 * @param locality
	 *            the locality.
	 * @param when
	 *            the time to get the response at.
	 * @return the model object key to use in the response.
	 * @throws IOException
	 *             something went wrong.
	 */
	@RequestMapping(value = "offset/{country}/{locality}", method = RequestMethod.GET)
	@ResponseBody
	public Integer getOffset(@PathVariable("country") String country,
			@PathVariable("locality") String locality,
			@RequestParam("when") Date when, HttpServletResponse httpResponse)
			throws IOException {

		String id = country + "/" + locality;
		Integer result = timeZoneServiceGateway.getOffset(id, when);

		if (result == null) {
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

		return result;
	}

	/**
	 * Translations.
	 * 
	 * @param request
	 *            request
	 * @param binder
	 *            binder
	 */
	@InitBinder
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) {
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		binder.registerCustomEditor(Date.class, null, new CustomDateEditor(
				parser, false));
	}

	@Inject
	public void setTimeZoneServiceGateway(
			TimeZoneServiceGateway timeZoneServiceGateway) {
		this.timeZoneServiceGateway = timeZoneServiceGateway;
	}

}
