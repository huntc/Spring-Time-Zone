package org.springframework.samples.springtz;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;

/**
 * A type of cron trigger that will fire immediately for the first time
 * something has been executed, and thereafter in accordance with the cron
 * schedule.
 * 
 * @author Christopher Hunt
 * 
 */
public class StartupCronTrigger extends CronTrigger {

	Logger logger = LoggerFactory.getLogger(StartupCronTrigger.class);

	public StartupCronTrigger(String cronExpression) {
		super(cronExpression);
	}

	@Override
	public Date nextExecutionTime(TriggerContext triggerContext) {
		Date nextExecutionTime;
		if (triggerContext.lastActualExecutionTime() != null) {
			nextExecutionTime = super.nextExecutionTime(triggerContext);
		} else {
			nextExecutionTime = new Date();
		}
		if (logger.isDebugEnabled()) {
			logger.debug(nextExecutionTime.toString());
		}
		return nextExecutionTime;
	}

}
