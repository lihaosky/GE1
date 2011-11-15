package master;

import java.util.HashMap;

/**
 * Track the jobs
 * @author lihao
 *
 */
public class JobTracker {
	private static HashMap<Integer, Job> jobMap = new HashMap<Integer, Job>();  //Store map of jobs
	
	public static void addJob(int jobID, Job job) {
		jobMap.put(jobID, job);
	}
	
	public static Job getJob(int jobID) {
		return jobMap.get(jobID);
	}

}
