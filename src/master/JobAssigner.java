package master;

import java.rmi.Remote;
import java.rmi.RemoteException;

import slave.AssignmentHandler;

/**
 * Remote interface
 * @author lihao
 *
 */
public interface JobAssigner extends Remote {
	/**
	 * Master upload data to slave
	 * @param jobID            jobID
	 * @param fileName         file needed to upload to slave
	 * @return
	 * @throws RemoteException
	 */
	public byte[] uploadData(int jobID, String fileName) throws RemoteException;
	
	/**
	 * Download result from slave. Called by slave. Use jobId to update the remaining replication.
	 * @param jobID             JobID
	 * @param fileName          file to be downloaded
	 * @param repNum            replication number completed
	 * @param assignmentHandler Assignment handler of slave
	 * @return
	 * @throws RemoteException
	 */
	public int downloadResult(int nodeID, int jobID, String fileName, int repNum, AssignmentHandler assignmentHandler) throws RemoteException;

}
