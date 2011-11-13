package slave;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;

import common.Directory;
import common.Parameters;

import master.JobAssigner;

public class Assignment extends Thread {
	private int nodeID;
	private int jobID;
	private String fileName;
	private ArrayList<Integer> repList;
	private JobAssigner jobAssigner;
	private static AssignmentHandler assignmentHandler;
	
	public Assignment(int nodeID, int jobID, String fileName, ArrayList<Integer> repList, JobAssigner jobAssigner) {
		this.jobID = jobID;
		this.nodeID = nodeID;
		this.fileName = fileName;
		this.repList = repList;
		this.jobAssigner = jobAssigner;
	}
	
	public void start() {
		File dir = new File(Parameters.slaveResultPath + "/");
		Directory.makeDir(dir);
		for (int i = 0; i < repList.size(); i++) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new File(Parameters.slaveResultPath + "/" + repList.get(i)+ "_result"));
			pw.println("good");
			jobAssigner.downloadResult(nodeID, jobID, repList.get(i)+ "_result", repList.get(i), assignmentHandler);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		}
	}
		
	public static void setAssignmentHandler(AssignmentHandler assign) {
		assignmentHandler = assign;
	}

}
