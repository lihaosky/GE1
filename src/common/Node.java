package common;

import java.rmi.RemoteException;
import java.util.ArrayList;

import master.JobAssigner;
import slave.AssignmentHandler;

/**
 * A slave node
 * @author lihao
 *
 */
public class Node {
	private static int nextNodeID = 1;
	private int nodeID;
	private String IPAddress;
	private AssignmentHandler assignmentHandler;
	private ArrayList<Integer> repList;
	private int status;
	public static int DEAD = 0;
	public static int AVAILABLE = 1;
	public static int BUSY = 2;
	
	public Node(String IPAddress, int status) {
		nodeID = nextNodeID++;
		this.IPAddress = IPAddress;
		this.status = status;
	}
	
	public AssignmentHandler findHandler() {
		return null;
	}
	
	public void setAssignmentHandler(AssignmentHandler assignmentHandler) {
		this.assignmentHandler = assignmentHandler;
	}
	
	public void setReplist(ArrayList<Integer> repList) {
		this.repList = repList;
	}
	
	public void addAssignment(int jobID, String fileName, ArrayList<Integer> repList, JobAssigner jobAssigner) {
		try {
			assignmentHandler.addAssignment(jobID, fileName, repList, jobAssigner);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
