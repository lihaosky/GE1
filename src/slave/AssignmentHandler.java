package slave;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import master.JobAssigner;

/**
 * Remote interface for master to start assignment and get result
 * @author lihao
 *
 */
public interface AssignmentHandler extends Remote {
	/**
	 * Master call slave's remote object to call this method to start assignment in slave
	 * @param jobID         JobID
	 * @param fileName      File needed to fetch
	 * @param repList       Replications need to do
	 * @param jobAssigner   Get the master's job assigner. In order to upload data to master
	 * @throws RemoteException
	 */
	public int addAssignment(int nodeID, long jobID, ArrayList<Integer> repList, JobAssigner jobAssigner) throws RemoteException;
	/**
	 * Upload result to master
	 * @param jobID JobID
	 * @param repNum Replication number completed
	 * @return 
	 * @throws RemoteException
	 */
	public byte[] uploadResult(long jobID, int repNum) throws RemoteException;
	/**
	 * Add replication to this node in case of other node's failure
	 * @param repList List of replications added
	 * @throws RemoteException
	 */
	public void addRep(ArrayList<Integer> repList) throws RemoteException;
}
