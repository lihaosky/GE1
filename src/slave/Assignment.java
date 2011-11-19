package slave;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;

import common.FileOperator;
import common.Parameters;

import master.JobAssigner;

/**
 * Represents divisions assigned by master to execute
 * @author lihao
 *
 */
public class Assignment extends Thread {
	private int nodeID;
	private long jobID;
	private ArrayList<Integer> repList;
	private JobAssigner jobAssigner;
	private static AssignmentHandler assignmentHandler;
	
	/**
	 * Assignment constructor
	 * @param nodeID This nodeID
	 * @param jobID JobID
	 * @param repList List of replication needs to be done by this node
	 * @param jobAssigner JobAssigner of master. For calling download
	 */
	public Assignment(int nodeID, long jobID, ArrayList<Integer> repList, JobAssigner jobAssigner) {
		this.jobID = jobID;
		this.nodeID = nodeID;
		this.repList = repList;
		this.jobAssigner = jobAssigner;
	}
	
	/**
	 * Start the exectution of replication
	 */
	public void run() {
		for (int i = 0; i < this.getRepListSize(); i++) {
			int rep = this.getRep(i);
			
			//Create directory for replication 
			File file = new File(FileOperator.slaveRepPath(jobID, rep));
			FileOperator.makeDir(file);
			file = new File(FileOperator.slaveDataPath(jobID));
			
			//Unzip data.zip to replication directory
			FileOperator.unzipFile(file, FileOperator.slaveRepPath(jobID, rep));
			
			//Copy marsMain to replication directory
			FileOperator.cpFile(new File(Parameters.marsMainLocation), new File(FileOperator.slaveRepPath(jobID, rep) + "/" + "marsMain"));
			FileOperator.cpFile(new File(Parameters.marsMainCtlLocation), new File(FileOperator.slaveRepPath(jobID, rep) + "/" + "mars.ctl"));
			
			/*********************************************
			 * NEED TO EDIT mars.ctl                     *
			 ********************************************/
			
			//Fake!
			String resultPath = "/home/lihao/Desktop/GE_Project/p1/result.zip";
			FileOperator.cpFile(new File(resultPath), new File(FileOperator.slaveResultPath(jobID, rep)));
			try {
				jobAssigner.downloadResult(nodeID, jobID, rep, assignmentHandler);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			
		}
	}
	
	/**
	 * Get the replication list size
	 * @return Replication list size
	 */
	synchronized public int getRepListSize() {
		return repList.size();
	}
	
	/**
	 * Add a replication
	 * @param rep
	 */
	synchronized public void addRep(ArrayList<Integer> rep) {
		for (int i = 0; i < rep.size(); i++) {
			repList.add(rep.get(i));
		}
	}
	
	/**
	 * Get the ith replication number
	 * @param i
	 * @return
	 */
	synchronized public int getRep(int i) {
		return repList.get(i);
	}
	
	/**
	 * Set the assignment handler
	 * @param assign Assignment handler
	 */
	public static void setAssignmentHandler(AssignmentHandler assign) {
		assignmentHandler = assign;
	}
}
