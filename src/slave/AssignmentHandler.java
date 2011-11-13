package slave;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import master.JobAssigner;

public interface AssignmentHandler extends Remote {
	/**
	 * Master call slave's remote object to call this method to start assignment in slave
	 * @param jobID         JobID
	 * @param fileName      File needed to fetch
	 * @param repList       Replications need to do
	 * @param jobAssigner   Get the master's job assigner. In order to upload data to master
	 * @throws RemoteException
	 */
	public int addAssignment(int nodeID, int jobID, String fileName, ArrayList<Integer> repList, JobAssigner jobAssigner) throws RemoteException;
	public byte[] uploadResult(int jobID, int repNum, String fileName) throws RemoteException;
	public void addRep(ArrayList<Integer> repList) throws RemoteException;
}
