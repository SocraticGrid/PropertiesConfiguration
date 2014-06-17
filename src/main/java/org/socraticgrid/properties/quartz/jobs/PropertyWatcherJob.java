/**
 * 
 */
package org.socraticgrid.properties.quartz.jobs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.socraticgrid.properties.SGProperties;
import org.socraticgrid.properties.SGProperties.PROPS;

/**
 * @author steven
 *
 */
public class PropertyWatcherJob implements Job {

	Logger log = Logger.getLogger(PropertyWatcherJob.class);

	/**
	 * This looks strange but is intentional.
	 * The scheduler will create a new instance each time the Job is scheduled
	 *  but lastModified needs to keep its state across instances.
	 *  
	 */
	private static long lastModified;

	public PropertyWatcherJob() {
		//Check to see if lastModified has been initialized
		if(lastModified==0){
			Path path = Paths.get(SGProperties.getProperty(PROPS.PROPERTIES_FILE));
			File hnfsProperties = path.toFile();
//			if(log.isDebugEnabled()) {
//				log.debug("********************");
//				log.debug("\t\tWatching File - "+hnfsProperties.getAbsolutePath());
//				log.debug("\t\tFile - "+(hnfsProperties.exists()?" DOES ":" DOES NOT ")+"EXIST");				
//				log.debug("********************\n");
//			}
			if(hnfsProperties.exists()) {
				lastModified = hnfsProperties.lastModified();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		long currModification = lastModified;
		Path path = Paths.get(SGProperties.getProperty(PROPS.PROPERTIES_FILE));
		File hnfsProperties = path.toFile();
		if(hnfsProperties.exists()) {
			currModification = hnfsProperties.lastModified();
		}
		if(currModification != lastModified){
			lastModified = currModification;
			SGProperties.refreshProperties();
		}

	}

}
