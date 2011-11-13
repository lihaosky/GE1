package master;

import java.util.ArrayList;
import client.Client;
import common.Node;

/**
 * This class represents a job by a client
 * @author lihao
 *
 */
public class Job extends Thread {
	private static int nextJobID = 1;
	private int jobID;
	private Client client;
	private int repNum;
	private String fileName;
	private ArrayList<Node> slaveList;
	private static JobAssigner jobAssigner;
	/**
	 * Create new Job with number of replications, fileName and client obj
	 * @param repNum
	 * @param fileName
	 * @param client
	 */
	public Job(int repNum, String fileName, Client client) {
		this.repNum = repNum;
		this.fileName = fileName;
		this.client = client;
		jobID = nextJobID++;
	}
	
	/**
	 * Start the job
	 */
	public void start() {
		
	}
	
	/**
	 * Get the JobID
	 * @return
	 */
	public int getJobID() {
		return jobID;
	}
	
	/**
	 * Update the repList when result is fetched
	 * @param repNum
	 */
	public void updateRepList(int repNum) {
		
	}
	
	public static void setJobAssigner(JobAssigner jobAgn) {
		Job.jobAssigner = jobAgn;
	}
}
