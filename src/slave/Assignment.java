package slave;

import java.util.ArrayList;

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
	
	public Assignment(int nodeID, long jobID, ArrayList<Integer> repList, JobAssigner jobAssigner) {
		this.jobID = jobID;
		this.nodeID = nodeID;
		this.repList = repList;
		this.jobAssigner = jobAssigner;
	}
	
	public void start() {
		for (int i = 0; i < repList.size(); i++) {
			
		}
	}
		
	public static void setAssignmentHandler(AssignmentHandler assign) {
		assignmentHandler = assign;
	}
}
