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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.RendezvousChannel;
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

	/**
	 * Utility class for providing a future task to handle replies
	 * asynchronously.
	 */
	private class ReplyTask extends FutureTask<Message<?>> {
		public ReplyTask(final RendezvousChannel replyChannel) {
			super(new Callable<Message<?>>() {
				@Override
				public Message<?> call() throws Exception {
					return template.receive(replyChannel);
				}
			});
		}
	}

	/*
	 * The channels we're going to use.
	 */
	private AbstractMessageChannel idsRequestChannel;

	private AbstractMessageChannel offsetRequestChannel;

	/*
	 * How long we are wanting to wait on sends and receives.
	 */
	private static long sendReceiveTimeout = 2000L;

	/**
	 * The template to use for sending and receiving messages.
	 */
	private final MessagingTemplate template = new MessagingTemplate();

	/**
	 * A thread pool for processing replies. We'll limit this to 10 for now.
	 */
	private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

	public TimeZoneServiceController() {
		template.setReceiveTimeout(sendReceiveTimeout);
		template.setSendTimeout(sendReceiveTimeout);
	}

	/**
	 * Get the available ids.
	 * 
	 * @return a list of ids.
	 * @throws IOException
	 *             something went wrong.
	 * @throws ExecutionException
	 *             thread issue.
	 * @throws InterruptedException
	 *             thread issue.
	 */
	@RequestMapping(value = "ids", method = RequestMethod.GET)
	@ResponseBody
	public List<?> getAvailableIDs(HttpServletResponse httpResponse)
			throws IOException, InterruptedException, ExecutionException,
			TimeoutException {

		List<?> result = null;

		// Create a channel for receiving replies on and build our message
		// passing in this reply channel.
		final RendezvousChannel replyChannel = new RendezvousChannel();
		Message<String> request = MessageBuilder.withPayload("")
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel).build();

		// Start a task to wait on processing replies.
		FutureTask<Message<?>> replyTask = new ReplyTask(replyChannel);
		threadPool.execute(replyTask);

		// Dispatch the request.
		template.send(idsRequestChannel, request);

		// Wait for the reply and process it if we have one. Note that we will
		// not wait indefinitely here due to the reply task having a timeout on
		// its template receive method.
		Message<?> reply = replyTask.get();
		if (reply != null) {
			Object payload = reply.getPayload();
			if (payload instanceof List<?>) {
				result = (List<?>) reply.getPayload();
			}
		}

		// Pass back a 404 if we've got nothing to return.
		if (result == null) {
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

		return result;
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
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@RequestMapping(value = "offset/{country}/{locality}", method = RequestMethod.GET)
	@ResponseBody
	public Integer getOffset(@PathVariable("country") String country,
			@PathVariable("locality") String locality,
			@RequestParam("when") Date when, HttpServletResponse httpResponse)
			throws IOException, InterruptedException, ExecutionException {

		Integer result = null;

		RendezvousChannel replyChannel = new RendezvousChannel();
		String id = country + "/" + locality;
		Message<String> request = MessageBuilder.withPayload(id)
				.setHeader("when", when)
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel).build();

		FutureTask<Message<?>> replyTask = new ReplyTask(replyChannel);
		threadPool.execute(replyTask);

		template.send(offsetRequestChannel, request);

		Message<?> reply = replyTask.get();
		if (reply != null) {
			Object payload = reply.getPayload();
			if (payload instanceof Integer) {
				result = (Integer) payload;
			}
		}

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

	@Resource(name = "idsRequestChannel")
	public void setIdsRequestChannel(AbstractMessageChannel idsRequestChannel) {
		this.idsRequestChannel = idsRequestChannel;
	}

	@Resource(name = "offsetRequestChannel")
	public void setOffsetRequestChannel(
			AbstractMessageChannel offsetRequestChannel) {
		this.offsetRequestChannel = offsetRequestChannel;
	}

}
