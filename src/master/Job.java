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
	}
	
	/**
	 * Start the job
	 */
	public void run() {
	
		slaveList = NodeManager.getNodes(time);
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
		System.out.println("Job " + jobID + " started in slaves!");
		
		//Keep heartbeating if not all the nodes finished
		while (true) {
			//All replications has been finished
			if (this.checkNodeStatus()) {
				break;
			}
			
			ArrayList<Node> failedNode = new ArrayList<Node>(); //Failed nodes
			ArrayList<Integer> reAllocateRep = new ArrayList<Integer>();  //Replications of failed nodes
			
			for (int i = 0; i < slaveList.size(); i++) {
				Node node = slaveList.get(i);
				//Heartbeat fails
				if (!node.heartBeat()) {
					failedNode.add(node);
					node.setStatus(Node.DEAD);
					//Get remaining replication of this node
					ArrayList<Integer> remainRep = node.getRep();
					
					System.out.println("Remaining replications are:");
					for (int j = 0; j < remainRep.size(); j++) {
						System.out.print(remainRep.get(j) + " ");
						reAllocateRep.add(remainRep.get(j));
					}
					System.out.println();
					node.clearRep();
					node.halt();
				}
			}
			
			//Remove failed nodes
			for (int i = 0; i < failedNode.size(); i++) {
				slaveList.remove(failedNode.get(i));
			}
			
			//All nodes fail
			if (slaveList.size() == 0) {
				System.out.println("All nodes failed!");
				try {
					oos.writeObject(new ErrorCommand(Command.ErrorMessage, "All nodes failed!"));
					oos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			
			
			//Add remaining replications to working nodes
			//Assume they will not fail when adding replications...
			if (reAllocateRep.size() != 0) {
				for (int i = 0; i < slaveList.size(); i++) {
					ArrayList<Integer> taskList = new ArrayList<Integer>();
					for (int j = i; j < reAllocateRep.size(); j++) {
						if (j % slaveList.size() == i) {
							System.out.println("Add replication " + reAllocateRep.get(j) + " to slave " + slaveList.get(i).getNodeID());
							taskList.add(reAllocateRep.get(j));
						}
					}
					slaveList.get(i).addRep(taskList); //Assume this will not fail...
				}
			}
			
			//Wait for 2 seconds before next heartbeat
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Job is done!");
		
		//Stop all slaves
		for (int i = 0; i < slaveList.size(); i++) {
			slaveList.get(i).finish();
		}
		
		//Edit mars-out.ctl
		System.out.println("Edit mars-out.ctl...");
		if (!FileOperator.editMarsoutCtl(new File(master.Parameters.masterResultPath + "/" + jobID + "/mars-out.ctl"), repNum)) {
			System.out.println("Edit mars-out.ctl error!");
			try {
				oos.writeObject(new ErrorCommand(Command.ErrorMessage, "Edit mars-out.ctl error!"));
				oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		//Merge
		System.out.println("Merge results...");
		try {
			Process p = Runtime.getRuntime().exec("./marsOut mars-out.ctl", null, new File(master.Parameters.masterResultPath + "/" + jobID));
			p.waitFor();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Zip results
		System.out.println("Zip results...");
		String[] extraFiles = {"marsOut", "mars-out.ctl"};
		if (!FileOperator.zipExcludeFile(new File(master.Parameters.masterResultPath + "/" + jobID), extraFiles)) {
			System.out.println("Zip results error!");
			try {
				oos.writeObject(new ErrorCommand(Command.ErrorMessage, "zip results error!"));
				oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		//Upload result to client
		File file = new File(Parameters.masterResultPath + "/" + jobID + "/" + common.Parameters.resultFileName);
		if (!file.exists()) {
			System.out.println("File " + file.getAbsolutePath() + " doesn't exist!");
			try {
				oos.writeObject(new ErrorCommand(Command.ErrorMessage, "Result file doesn't exist!"));
				oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
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
				if ((repList.get(j) % slaveList.size()) == ((i + 1) % slaveList.size())) {
					taskList.add(repList.get(j));
					index.add(repList.get(j));
				}
			}
			if (slaveList.get(i).addAssignment(jobID, taskList) != Message.OK) {
				System.out.println("Add assignment to slave " + slaveList.get(i).getNodeID() + " error!");
				slaveList.get(i).clearRep();
				failedNode.add(slaveList.get(i));
				slaveList.get(i).setStatus(Node.DEAD);
			} else {
				slaveList.get(i).start();
				System.out.println("Slave " + slaveList.get(i).getNodeID() + " Started!");
				System.out.println("Slave " + slaveList.get(i).getNodeID() + " has replications:");
				for (int j = 0; j < taskList.size(); j++) {
					System.out.print(taskList.get(j) + " ");
				}
				System.out.println();
				//Remove added replication number
				for (int k = 0; k < index.size(); k++) {
					for (int j = 0; j < repList.size(); j++) {
						if (repList.get(j) == index.get(k)) {
							repList.remove(j);
							break;
						}
					}
				}
			}
		}
		
		//All replications has been added
		if (repList.size() == 0) {
			System.out.println("All replications added!");
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
	 */
	synchronized public void updateRepList() {
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
