package slave;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import common.Command;
import common.DownloadRepCommand;
import common.FileOperator;

/**
 * Represents divisions assigned by master to execute
 * @author lihao
 *
 */
public class Assignment extends Thread {
	private long jobID;
	private ArrayList<Integer> repList;
	private Socket masterSocket;
	/**
	 * Assignment constructor
	 * @param nodeID This nodeID
	 * @param jobID JobID
	 * @param repList List of replication needs to be done by this node
	 * @param jobAssigner JobAssigner of master. For calling download
	 */
	public Assignment(int nodeID, long jobID, ArrayList<Integer> repList, Socket masterSocket) {
		this.jobID = jobID;
		this.repList = repList;
		this.masterSocket = masterSocket;
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
			FileOperator.cpFile(new File(slave.Parameters.marsMainLocation), new File(FileOperator.slaveRepPath(jobID, rep) + "/" + "marsMain"));
			FileOperator.cpFile(new File(slave.Parameters.marsMainCtlLocation), new File(FileOperator.slaveRepPath(jobID, rep) + "/" + "mars.ctl"));
			
			/*********************************************
			 * NEED TO EDIT mars.ctl                     *
			 ********************************************/
			
			//Fake!
			String resultPath = "/home/lihao/Desktop/GE_Project/p1/result.zip";
			FileOperator.cpFile(new File(resultPath), new File(FileOperator.slaveResultPath(jobID, rep)));
			System.out.println("Upload replication " + rep + " to master...");
			String filePath = FileOperator.slaveResultPath(jobID, rep);
			file = new File(filePath);
			try {
				ObjectOutputStream oos = new ObjectOutputStream(masterSocket.getOutputStream());
				oos.writeObject(new DownloadRepCommand(Command.DownloadRepCommand, rep, file.length()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!FileOperator.uploadFile(masterSocket, filePath, file.length())) {
				System.out.println("Upload replication to master error!");
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
	}
}
