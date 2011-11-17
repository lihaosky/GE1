package slave;

import java.util.HashMap;

/**
 * Track the assignment
 * @author haoli
 *
 */
public class AssignmentTracker {
	private static HashMap<Long, Assignment> assignmentMap = new HashMap<Long, Assignment>();  //Store map of jobs
	
	public static void addAssignment(long assignID, Assignment assignment) {
		assignmentMap.put(assignID, assignment);
	}
	
	public static Assignment getAssignment(long assignID) {
		return assignmentMap.get(assignID);
	}
}
