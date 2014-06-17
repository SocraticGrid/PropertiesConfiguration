/**
 * 
 */
package org.socraticgrid.properties.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.socraticgrid.properties.SGProperties;
import org.socraticgrid.properties.SGProperties.PROPS;
import org.socraticgrid.properties.quartz.jobs.PropertyWatcherJob;

/**
 * @author steven
 *
 */
public class QuartzListener extends QuartzInitializerListener {
	
	private static Logger log = Logger.getLogger(QuartzListener.class);

	private static ServletContext servletContext;
	public static String getServletContextPath() {
		if(servletContext == null){
			return null;
		}
		return servletContext.getServletContextName().replaceAll("/","");
	}

	
	private static Map<String,JobKey> jobKeys = new ConcurrentHashMap<>(); 
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		super.contextInitialized(sce);
		QuartzListener.servletContext = sce.getServletContext();
		log.info("CATALNA.BASE : "+System.getProperty("catalina.base"));
		
		//Init and start the scheduler
		StdSchedulerFactory factory = QuartzListener.getSchedulerFactory();
		Scheduler scheduler = null;
		if(factory != null) {
			try {
				scheduler = factory.getScheduler();
				scheduler.start();
			} catch (SchedulerException e) {
				log.error("*******************");
				log.error("\tERROR : QuartzScheduler cannot be INITIALIZED !!!!!");
				log.error("*******************");
				log.error(e,e);
			}
		}
		
		//Init and start a scheduler to monitor External.properties file
		String schedule = SGProperties.getProperty(PROPS.PROPERTIESFILE_SCHEDULE);
		if( scheduler!=null && !schedule.isEmpty()) {
			try {
				JobDetail job = newJob(PropertyWatcherJob.class)
						.withIdentity("Properties Watcher Job","properties_quartz1")
						.build();
				Trigger trigger;
				trigger = newTrigger()
						.withIdentity("trigger1","properties_quartz1")
						.withSchedule(cronSchedule(new CronExpression(schedule)))
						.build();

				scheduler.scheduleJob(job,trigger);
				jobKeys.put("Properties Watcher Job",job.getKey());
				
				log.info("*******************");
				log.info("\t\n Properties Watcher Job scheduled for - "+schedule);
				log.info("*******************");
			} catch (ParseException | SchedulerException e) {
				log.error("*******************");
				log.error("\tERROR : Properties Watcher Job COULD NOT BE scheduled !!!!!");
				log.error("*******************");
				log.error(e,e);
			}
		}
	}
	
		
	
	public static StdSchedulerFactory getSchedulerFactory() {
		if(servletContext != null) {
			return (StdSchedulerFactory)servletContext.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		}
		else {
			return null;
		}
	}	
}
