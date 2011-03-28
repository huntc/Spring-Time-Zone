package com.classactionpl.tz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

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

	/**
	 * The time zone service to use.
	 */
	private TimeZoneService timeZoneService;

	/**
	 * Get the available ids.
	 * 
	 * @return a list of ides.
	 */
	@RequestMapping(value = "ids", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getAvailableIDs() {
		return timeZoneService.getAvailableIDs();
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

		return timeZoneService.getOffset(country + "/" + locality, when);
	}

	public TimeZoneService getTimeZoneService() {
		return timeZoneService;
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
	public void setTimeZoneService(TimeZoneService timeZoneService) {
		this.timeZoneService = timeZoneService;
	}

}
