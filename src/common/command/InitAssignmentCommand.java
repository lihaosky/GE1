package common.command;

import java.util.ArrayList;


public class InitAssignmentCommand extends Command {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4896991835733163955L;
	public long jobID;
	public int nodeID;
	public ArrayList<Integer> repList;
	public long fileLength;
	
	public InitAssignmentCommand(int id, long jobID, int nodeID, long fileLength, ArrayList<Integer> repList) {
		super(id);
		this.jobID = jobID;
		this.nodeID = nodeID;
		this.fileLength = fileLength;
		this.repList = repList;
	}
}
