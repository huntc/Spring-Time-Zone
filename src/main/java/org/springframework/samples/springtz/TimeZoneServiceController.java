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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.integration.Message;
import org.springframework.integration.channel.AbstractPollableChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
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

	/*
	 * The channels we're going to use.
	 */
	private AbstractPollableChannel idsRequestChannel;
	private AbstractPollableChannel offsetRequestChannel;

	private static long sendReceiveTimeout = 2000L;

	private final MessagingTemplate template = new MessagingTemplate();

	public TimeZoneServiceController() {
		template.setReceiveTimeout(sendReceiveTimeout);
		template.setSendTimeout(sendReceiveTimeout);
	}

	/**
	 * Get the available ids.
	 * 
	 * @return a list of ides.
	 */
	@RequestMapping(value = "ids", method = RequestMethod.GET)
	@ResponseBody
	public List<?> getAvailableIDs() {
		List<?> payloadList;

		Message<String> request = MessageBuilder.withPayload("").build();
		Message<?> reply = template.sendAndReceive(idsRequestChannel, request);

		if (reply != null) {
			payloadList = (List<?>) reply.getPayload();
		} else {
			payloadList = null;
		}

		return payloadList;
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
	 */
	@RequestMapping(value = "offset/{country}/{locality}", method = RequestMethod.GET)
	@ResponseBody
	public Integer getOffset(@PathVariable("country") String country,
			@PathVariable("locality") String locality,
			@RequestParam("when") Date when) {

		Integer result;

		String payload = country + "/" + locality;
		Message<String> request = MessageBuilder.withPayload(payload)
				.setHeader("when", when).build();

		Message<?> reply = template.sendAndReceive(offsetRequestChannel,
				request);
		if (reply != null) {
			result = (Integer) reply.getPayload();
		} else {
			result = null;
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

	@Resource(name = "idsRequestChannel")
	public void setIdsRequestChannel(AbstractPollableChannel idsRequestChannel) {
		this.idsRequestChannel = idsRequestChannel;
	}

	@Resource(name = "offsetRequestChannel")
	public void setOffsetRequestChannel(
			AbstractPollableChannel offsetRequestChannel) {
		this.offsetRequestChannel = offsetRequestChannel;
	}

}
