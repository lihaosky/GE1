package master;

import java.util.HashMap;

/**
 * Track the jobs
 * @author lihao
 *
 */
public class JobTracker {
	private static HashMap<Long, Job> jobMap = new HashMap<Long, Job>();  //Store map of jobs
	
	public static void addJob(long jobID, Job job) {
		jobMap.put(jobID, job);
	}
	
	public static Job getJob(long jobID) {
		return jobMap.get(jobID);
	}

}
