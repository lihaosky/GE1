package slave;

import java.util.HashMap;

/**
 * Track the assignment
 * @author haoli
 *
 */
public class AssignmentTracker {
	private static HashMap<Long, Assignment> assignmentMap = new HashMap<Long, Assignment>();  //Store map of jobs
	
	/**
	 * Add assignment
	 * @param assignID AssignmentID. Same as JobID
	 * @param assignment Assignment
	 */
	synchronized public static void addAssignment(long assignID, Assignment assignment) {
		assignmentMap.put(assignID, assignment);
	}
	
	/**
	 * Get assignment
	 * @param assignID AssignmentID
	 * @return
	 */
	synchronized public static Assignment getAssignment(long assignID) {
		return assignmentMap.get(assignID);
	}
	
	synchronized public static void removeAssignment(long assignID) {
		assignmentMap.remove(assignID);
	}
}
