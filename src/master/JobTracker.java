package master;

import java.util.HashMap;

/**
 * Track the jobs
 * @author lihao
 *
 */
public class JobTracker {
	private static HashMap<Long, Job> jobMap = new HashMap<Long, Job>();  //Store map of jobs
	
	/**
	 * Add job
	 * @param jobID JobID
	 * @param job Job
	 */
	public static void addJob(long jobID, Job job) {
		jobMap.put(jobID, job);
	}
	
	/**
	 * Get job
	 * @param jobID JobID
	 * @return Job
	 */
	public static Job getJob(long jobID) {
		return jobMap.get(jobID);
	}

}
