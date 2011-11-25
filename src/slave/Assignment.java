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
	private ObjectOutputStream oos;
	
	/**
	 * Assignment constructor
	 * @param nodeID This nodeID
	 * @param jobID JobID
	 * @param repList List of replication needs to be done by this node
	 * @param jobAssigner JobAssigner of master. For calling download
	 */
	public Assignment(int nodeID, long jobID, ArrayList<Integer> repList, Socket masterSocket, ObjectOutputStream oos) {
		this.jobID = jobID;
		this.repList = repList;
		this.oos = oos;
		this.masterSocket = masterSocket;
	}
	
	/**
	 * Start the exectution of replication
	 */
	public void run() {
		for (int i = 0; i < this.getRepListSize(); i++) {
			int rep = this.getRep(i);
			
			//Create directory for replication 
			System.out.println("Create directory for replication " + rep + "...");
			File file = new File(FileOperator.slaveRepPath(jobID, rep));
			FileOperator.makeDir(file);
			file = new File(FileOperator.slaveDataPath(jobID));
			
			//Unzip data.zip to replication directory
			System.out.println("Unzip data file...");
			if (!FileOperator.unzipFile(file, FileOperator.slaveRepPath(jobID, rep))) {
				System.out.println("Unzip file error!");
			}
			
			//Copy marsMain to replication directory
			System.out.println("Copy marsMain...");
			if (!FileOperator.cpFile(new File(slave.Parameters.marsMainLocation), new File(FileOperator.slaveRepPath(jobID, rep) + "/" + "marsMain"))) {
				System.out.println("Copy file error!");
			}
			//Mars.ctl will be provided by client
			//FileOperator.cpFile(new File(slave.Parameters.marsMainCtlLocation), new File(FileOperator.slaveRepPath(jobID, rep) + "/" + "mars.ctl"));
			
			/*********************************************
			 * NEED TO EDIT mars.ctl                     *
			 ********************************************/
			//Edit the mars.ctl
			System.out.println("Edit the mars.ctl file...");
			if (!FileOperator.editMarsCtl(new File(FileOperator.slaveRepPath(jobID, rep) + "/" + "mars.ctl"), rep)) {
				System.out.println("Edit mars.ctl in " + rep + " error!");
			}
			
			//Start execution
			System.out.println("Start execution of replication " + rep + "...");
			try {
				Process p = Runtime.getRuntime().exec("./marsMain mars.ctl", null, new File(FileOperator.slaveRepPath(jobID, rep)));
				p.waitFor();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//Zip the output file and send it to master
			System.out.println("Zip result...");
			String[] extraFile = new String[common.Parameters.neededInputFiles.length + 1];
			extraFile[common.Parameters.neededInputFiles.length] = "marsMain";
			if (!FileOperator.zipExcludeFile(new File(FileOperator.slaveRepPath(jobID, rep)), extraFile)) {
				System.out.println("Zip result error!");
			}
			
			/*
			//Fake!
			String resultPath = "/home/lihao/Desktop/GE_Project/p1/result.zip";
			FileOperator.cpFile(new File(resultPath), new File(FileOperator.slaveResultPath(jobID, rep)));*/
			
			System.out.println("Upload replication " + rep + " to master...");
			String filePath = FileOperator.slaveResultPath(jobID, rep);
			file = new File(filePath);
			try {
				oos.writeObject(new DownloadRepCommand(Command.DownloadRepCommand, rep, file.length()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!FileOperator.uploadFile(masterSocket, filePath, file.length())) {
				System.out.println("Upload replication to master error!");
			}
		}
		try {
			masterSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
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
}
