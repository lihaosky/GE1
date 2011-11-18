package master;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import common.Parameters;

import slave.AssignmentHandler;

/**
 * Contains node information
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
	
	/**
	 * @param IPAddress Node IP address
	 * @param status    Node status: Available, Dead or Busy
	 */
	public Node(String IPAddress, int status) {
		nodeID = getNextNodeID();
		this.IPAddress = IPAddress;
		this.status = status;
	}
	
	/**
	 * Find slave handler
	 * @return slave handler
	 */
	public AssignmentHandler findHandler() {
		try {
			Registry registry = LocateRegistry.getRegistry("localhost");
			assignmentHandler = (AssignmentHandler)registry.lookup(Parameters.slaveHandlerName);
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Set slave handler
	 * @param assignmentHandler Assignment Handler
	 */
	public void setAssignmentHandler(AssignmentHandler assignmentHandler) {
		this.assignmentHandler = assignmentHandler;
	}
	
	/**
	 * Set replication list to slave
	 * @param repList Replication list
	 */
	public void setReplist(ArrayList<Integer> repList) {
		this.repList = repList;
	}
	
	/**
	 * Add assignment
	 * @param jobID JobID
	 * @param repList Replication list
	 * @param jobAssigner Job Assigner
	 */
	public void addAssignment(long jobID, ArrayList<Integer> repList, JobAssigner jobAssigner) {
		try {
			setReplist(repList);
			assignmentHandler.addAssignment(nodeID, jobID, repList, jobAssigner);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get nodeID
	 * @return NodeID
	 */
	public int getNodeID() {
		return nodeID;
	}
	
	/**
	 * Remove a replication from this node when it is done
	 * @param repNum
	 */
	public void removeRep(int repNum) {
		for (int i = 0; i < repList.size(); i++) {
			if (repList.get(i) == repNum) {
				repList.remove(i);
			}
		}
	}
	
	/**
	 * Check if there is still replication pending in this node
	 * @return
	 */
	public boolean isEmptyRep() {
		return repList.size() == 0 ? true : false;
	}
	
	/**
	 * Get IP address of this node
	 * @return
	 */
	public String getIPAddr() {
		return IPAddress;
	}
	
	/**
	 * Get status of node
	 * @return
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * Get next nodeID, this needs to be atomic
	 * @return
	 */
	synchronized static int getNextNodeID() {
		return nextNodeID++;
	}
}
