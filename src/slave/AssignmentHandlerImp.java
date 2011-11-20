package slave;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import common.FileOperator;
import common.Message;
import master.JobAssigner;

/**
 * This class handle request from master to initiate assignment and upload result
 * @author lihao
 *
 */
public class AssignmentHandlerImp extends UnicastRemoteObject implements AssignmentHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected AssignmentHandlerImp() throws RemoteException {
		super();
	}

	/**
	 * Download data from master, get the replication this slave need to do
	 */
	public int addAssignment(int nodeID, long jobID, ArrayList<Integer> repList, JobAssigner jobAssigner)
			throws RemoteException {
		//Make data directory
		File file = new File(slave.Parameters.slaveDataPath + "/" + jobID);
		if (!FileOperator.makeDir(file)) {
			return Message.MkDirError;
		}
		
		//Store the file from master
		byte[] bytes = jobAssigner.uploadData(jobID);
		if (bytes == null) {
			System.err.println("No file uploaded from master!");
			return Message.UploadError;
		}
		
		//Store file
		String filePath = FileOperator.slaveDataPath(jobID);
		FileOperator.storeFile(filePath, bytes);
		
		//Start the assignment
		Assignment assignment = new Assignment(nodeID, jobID, repList, jobAssigner);
		assignment.start();
		AssignmentTracker.addAssignment(jobID, assignment);
		
		return Message.OK;
	}

	/**
	 * Upload result to master
	 */
	public byte[] uploadResult(long jobID, int repNum)
			throws RemoteException {
		String filePath = FileOperator.slaveResultPath(jobID, repNum);
		System.out.println(filePath);
		File file = new File(filePath);
		System.out.println(file.length());
		return FileOperator.getBytes(filePath);
	}

	/**
	 * Add new replications in case of other's failure
	 */
	public void addRep(long jobID, ArrayList<Integer> repList) throws RemoteException {
		Assignment assignment = AssignmentTracker.getAssignment(jobID);
		assignment.addRep(repList);
	}
}
