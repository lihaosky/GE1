package master;

import java.util.ArrayList;
import java.util.Date;

import client.Client;

/**
 * This class represents a job by a client
 * @author lihao
 *
 */
public class Job extends Thread {
	private static long nextJobID = 1;
	private long jobID;
	private Client client;
	private int repNum;
	private ArrayList<Node> slaveList;
	private static JobAssigner jobAssigner;
	//public boolean isJobDone = false;
	public Object isJobDone = new Object();   //Used to wait for job to be done
	public int time;
	public Date startTime;
	public Date endTime;
	
	/**
	 * Create new Job with number of replications, fileName and client obj
	 * @param repNum
	 * @param fileName
	 * @param client
	 */
	public Job(int repNum, Client client, int time) {
		this.repNum = repNum;
		this.client = client;
		this.time = time;
		jobID = getNextJobID();
	}
	
	/**
	 * Start the job
	 */
	public void start() {
		/*ArrayList<Integer> taskList = new ArrayList<Integer>();
		for (int i = 1; i <= repNum; i++) {
			taskList.add(i);
		}
		File dir = new File(Parameters.masterResultPath);
		Directory.makeDir(dir);
		slaveList = NodeManager.getNodes(1);
		slaveList.get(0).setReplist(taskList);
		slaveList.get(0).addAssignment(jobID, taskList, jobAssigner);
		
		try {
			isJobDone.wait();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		//MERGE RESULT: TO BE DONE
		
		try {
			client.downloadResult();
		} catch (RemoteException e) {
			e.printStackTrace();
		}*/
		System.out.println("Job started!");
	}
	
	/**
	 * Get the JobID
	 * @return
	 */
	public long getJobID() {
		return jobID;
	}
	
	/**
	 * Update the repList when result is fetched
	 * @param repNum
	 */
	public void updateRepList(int nodeID, int repNum) {
		for (int i = 0; i < slaveList.size(); i++) {
			if (slaveList.get(i).getNodeID() == nodeID) {
				slaveList.get(i).removeRep(repNum);
			}
		}
	}
	
	/**
	 * Check if all nodes finishes their assignment
	 * @return
	 */
	public boolean checkNodeStatus() {
		for (int i = 0; i < slaveList.size(); i++) {
			if (!slaveList.get(i).isEmptyRep()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Set the job assigner for master
	 * @param jobAgn job assigner
	 */
	public static void setJobAssigner(JobAssigner jobAgn) {
		Job.jobAssigner = jobAgn;
	}
	
	/**
	 * Get next jobID, this need to be atomic
	 * @return
	 */
	synchronized static long getNextJobID() {
		return nextJobID++;
	}
}
