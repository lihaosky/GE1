package master;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import common.Command;
import common.DownloadCommand;
import common.FileOperator;
import common.Message;

/**
 * This class represents a job by a client
 * @author lihao
 *
 */
public class Job extends Thread {
	private static long nextJobID = 1;
	private long jobID;
	private ObjectOutputStream oos;
	private Socket clientSocket;
	private int repNum;
	private ArrayList<Node> slaveList;
	public Object isJobDone = new Object();   //Used to wait for job to be done
	public int time;
	public Date startTime;
	public Date endTime;
	private int finishedRep;
	
	/**
	 * Create new Job with number of replications, fileName and client obj
	 * @param repNum
	 * @param fileName
	 * @param client
	 */
	public Job(int repNum, Socket clientSocket, ObjectOutputStream oos, int time) {
		this.repNum = repNum;
		this.time = time;
		this.oos = oos;
		this.clientSocket = clientSocket;
		jobID = getNextJobID();
		finishedRep = 0;
	//	this.isJobDone = isJobDone;
	}
	
	/**
	 * Start the job
	 */
	public void run() {
		slaveList = NodeManager.getNodes(1);
		if (slaveList == null) {
			System.out.println("Can't get slave!");
			return;
		}
		
		ArrayList<Integer> taskList = new ArrayList<Integer>();
		for (int i = 1; i <= repNum; i++) {
			taskList.add(i);
		}

		if (slaveList.get(0).addAssignment(jobID, taskList) != Message.OK) {
			System.out.println("Add assignment to slave error!");
			slaveList.get(0).clearRep();
			return;
		} else {
			slaveList.get(0).start();
		}
		
		System.out.println("Job " + jobID + " started in slaves!");
		
		try {
			synchronized(isJobDone) {
				isJobDone.wait();
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("Job is done!");
		
		
		//MERGE RESULT: TO BE DONE
		
		//Upload result to client
		File file = new File(Parameters.masterResultPath + "/" + jobID + "/" + common.Parameters.resultFileName);
		long fileLength = file.length();
		try {
			oos.writeObject(new DownloadCommand(Command.DownloadCommand, fileLength));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!FileOperator.uploadFile(clientSocket, file.getAbsolutePath(), fileLength)) {
			System.out.println("Upload result to client error!");
		} else {
			System.out.println("Result uploaded to client!");
		}
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
	 * @param nodeID NodeID which needs update
	 * @param repNum Replication number to be removed
	 */
	synchronized public void updateRepList(int nodeID, int repNum) {
		for (int i = 0; i < slaveList.size(); i++) {
			if (slaveList.get(i).getNodeID() == nodeID) {
				slaveList.get(i).removeRep(repNum);
			}
		}
		finishedRep++;
	}
	
	/**
	 * Check if all nodes finishes their assignment
	 * @return
	 */
	synchronized public boolean checkNodeStatus() {
		for (int i = 0; i < slaveList.size(); i++) {
			if (!slaveList.get(i).isEmptyRep()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Get next jobID, this need to be atomic
	 * @return
	 */
	synchronized static long getNextJobID() {
		return nextJobID++;
	}
	
	public int getFinishedRep() {
		return finishedRep;
	}
}
