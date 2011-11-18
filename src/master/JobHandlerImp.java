package master;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import client.Client;
import common.FileOperator;
import common.Message;
import common.Parameters;

/**
 * Handle client job request
 * @author lihao
 *
 */
public class JobHandlerImp extends UnicastRemoteObject implements JobHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	protected JobHandlerImp() throws RemoteException {
		super();
	}

	/**
	 * Client add job
	 * @param repNum Number of replications
	 * @param client Remote client object. For calling download
	 * @param time Time limit provide by client
	 */
	public long addJob(int repNum, Client client, int time) throws RemoteException {
		System.out.println("Get client request!");
		
		Job job = new Job(repNum, client, time);    //Create a new job
		long jobID = job.getJobID();
	    
	
		//Make directory according to the unique jobID
		File dir = new File(Parameters.masterDataPath + "/" + jobID);
		
		//Make data directory for this JobID
		if (!FileOperator.makeDir(dir)) {
			return Message.MkDirError;
		}
		//Make result directory for this JobID
		if (!FileOperator.makeDir(new File(Parameters.masterResultPath + "/" + jobID))) {
			return Message.MkDirError;
		}
		//Copy marsOut to this JobID
		if (!FileOperator.cpFile(new File(Parameters.marsOutLocation), new File(Parameters.masterResultPath + "/" + jobID + "/" + "marsOut"))) {
			return Message.CopyFileError;
		}
		//Copy marsOut control file
		if (!FileOperator.cpFile(new File(Parameters.marsOutCtlLocation), new File(Parameters.masterResultPath + "/" + jobID + "/" + "mars-out.ctl"))) {
			return Message.CopyFileError;
		}
		
		//Store the file
		byte[] bytes = client.uploadData();
		if (bytes == null) {
			System.err.println("No file uploaded!");
			return Message.UploadError;
		}
		String filePath = Parameters.masterDataPath + "/" + jobID + "/" + Parameters.dataFileName;
		if (!FileOperator.storeFile(filePath, bytes)) {
			return Message.StoreFileError;
		}
		
		job.start();   //Everything goes well, start the job
		JobTracker.addJob(jobID, job);   //Add the job to job tracker
		
		return job.getJobID();
	}
	
	/**
	 * Find job status
	 * @param jobID JobID
	 */
	public void getJobStatus(long jobId) throws RemoteException {
		
	}
	
	/**
	 * Upload result to client
	 */
	public byte[] uploadResult() throws RemoteException {
		String filePath = Parameters.masterResultPath + "/" + Parameters.resultFileName;
		return FileOperator.getBytes(filePath);
	}
	

}
