package slave;

import java.util.HashMap;

/**
 * Track the assignment
 * @author haoli
 *
 */
public class AssignmentTracker {
	private static HashMap<Integer, Assignment> assignmentMap = new HashMap<Integer, Assignment>();  //Store map of jobs
	
	public static void addAssignment(int assignID, Assignment assignment) {
		assignmentMap.put(assignID, assignment);
	}
	
	public static Assignment getAssignment(int assignID) {
		return assignmentMap.get(assignID);
	}
}
