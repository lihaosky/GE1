package master;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import client.Client;
import common.Directory;
import common.Node;
import common.NodeManager;
import common.Parameters;

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
	//public boolean isJobDone = false;
	public Object isJobDone = new Object();   //Used to wait for job to be done
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
		jobID = getNextJobID();
	}
	
	/**
	 * Start the job
	 */
	public void start() {
		ArrayList<Integer> taskList = new ArrayList<Integer>();
		for (int i = 1; i <= repNum; i++) {
			taskList.add(i);
		}
		File dir = new File(Parameters.masterResultPath);
		Directory.makeDir(dir);
		slaveList = NodeManager.getNodes(1);
		slaveList.get(0).setReplist(taskList);
		slaveList.get(0).addAssignment(jobID, fileName, taskList, jobAssigner);
		
		try {
			isJobDone.wait();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		//MERGE RESULT: TO BE DONE
		
		try {
			client.downloadResult("result.txt");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
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
	synchronized static int getNextJobID() {
		return nextJobID++;
	}
}
