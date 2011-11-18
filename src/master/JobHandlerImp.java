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
	 */
	public long addJob(int repNum, Client client, int time) throws RemoteException {
		System.out.println("Get client request!");
		
		Job job = new Job(repNum, client, time);    //Create a new job
		long jobID = job.getJobID();
	    
		/*
		 * Make directory according to the unique jobID
		 */
		File dir = new File(Parameters.masterDataPath + "/" + jobID);
		
		if (!FileOperator.makeDir(dir)) {
			return Message.MkDirError;
		}
		if (!FileOperator.makeDir(new File(Parameters.masterResultPath + "/" + jobID))) {
			return Message.MkDirError;
		}
		if (!FileOperator.cpFile(new File(Parameters.marsOutLocation), new File(Parameters.masterResultPath + "/" + jobID + "/" + "marsOut"))) {
			return Message.CopyFileError;
		}
		/*
		 * Store the file
		 */
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
