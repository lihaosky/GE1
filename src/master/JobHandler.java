package master;

import java.rmi.Remote;
import java.rmi.RemoteException;
import client.Client;

/**
 * Remote Class called by client to initiate a task
 * @author lihao
 *
 */
public interface JobHandler extends Remote {
	/**
	 * 
	 * @param repNum           Replication number specified by client
	 * @param fileName         File name provide by client
	 * @param client           Client object used for callback
	 * @return                 Unique jobID to client
	 * @throws RemoteException
	 */
	public int addJob(int repNum, String fileName, Client client) throws RemoteException;
	
	/**
	 * Upload result to client
	 * @return
	 * @throws RemoteException
	 */
	public byte[] uploadResult() throws RemoteException;
	/**
	 * Get the job status according to jobId
	 * @param jobId
	 * @throws RemoteException
	 */
	public void getJobStatus(int jobId) throws RemoteException;
	
	
	//Can have other methods like abort job, etc.
}
