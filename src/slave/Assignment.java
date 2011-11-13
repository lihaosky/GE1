package slave;

import java.util.ArrayList;
import master.JobAssigner;

public class Assignment extends Thread {
	private int jobID;
	private String fileName;
	private ArrayList<Integer> repList;
	private JobAssigner jobAssigner;
	
	public Assignment(int jobID, String fileName, ArrayList<Integer> repList, JobAssigner jobAssigner) {
		this.jobID = jobID;
		this.fileName = fileName;
		this.repList = repList;
		this.jobAssigner = jobAssigner;
	}
	
	public void start() {
		
	}
}
