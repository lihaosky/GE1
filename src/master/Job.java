package master;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import common.FileOperator;
import common.Message;
import common.command.Command;
import common.command.DownloadCommand;
import common.command.ErrorCommand;

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
	
		slaveList = NodeManager.getNodes(2);
		if (slaveList == null) {
			System.out.println("Can't get slave!");
			try {
				oos.writeObject(new ErrorCommand(Command.ErrorMessage, "Can't find nodes!"));
				oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		if (!allocateRep()) {
			System.out.println("Allocate assignments failed in all slaves!");
			try {
				oos.writeObject(new ErrorCommand(Command.ErrorMessage, "Allocate job failed!"));
				oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		//Heart beat thing
		
		
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
			oos.flush();
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
	 * Allocate replications to slaves
	 * @return False if all the slaves die
	 */
	public boolean allocateRep() {
		ArrayList<Integer> repList = new ArrayList<Integer>();  //Replication number list
		ArrayList<Node> failedNode = new ArrayList<Node>();     //Failed node list
		
		for (int i = 1; i <= repNum; i++) {
			repList.add(i);
		}
		
		for (int i = 0; i < slaveList.size(); i++) {
			ArrayList<Integer> index = new ArrayList<Integer>();   //Allocated replication index
			ArrayList<Integer> taskList = new ArrayList<Integer>();//Replication list for one node
			
			for (int j = 0; j < repList.size(); j++) {
				if (repList.get(j) % slaveList.size() == (i + 1)) {
					taskList.add(repList.get(j));
					index.add(j);
				}
			}
			if (slaveList.get(i).addAssignment(jobID, taskList) != Message.OK) {
				System.out.println("Add assignment to slave error!");
				slaveList.get(i).clearRep();
				failedNode.add(slaveList.get(i));
			} else {
				slaveList.get(i).start();
				System.out.println("Slave: " + (i+1) + " Started!");
				//Remove added replication number
				for (int k = 0; k < index.size(); k++) {
					repList.remove(index.get(k));
				}
			}
		}
		
		//All replications has been added
		if (repList.size() == 0) {
			return true;
		}
		
		//Remove failed node
		for (int i = 0; i < failedNode.size(); i++) {
			slaveList.remove(failedNode.get(i));
		}
		//All nodes fail...
		if (slaveList.size() == 0) {
			return false;
		}
		
		//Add remaining replications to working nodes
		//Assume they will not fail when adding replications...
		for (int i = 0; i < slaveList.size(); i++) {
			ArrayList<Integer> taskList = new ArrayList<Integer>();
			for (int j = i; j < repList.size(); j++) {
				if (j % slaveList.size() == i) {
					taskList.add(repList.get(j));
				}
				slaveList.get(i).addRep(taskList); //Assume this will not fail...
			}
		}
		
		return true;
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
