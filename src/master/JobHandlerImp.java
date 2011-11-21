package master;

import java.io.File;
import java.rmi.RemoteException;
import client.Client;
import common.FileOperator;
import common.Message;

/**
 * Handle client job request
 * @author lihao
 *
 */
public class JobHandlerImp implements JobHandler {

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
		File dir = new File(master.Parameters.masterDataPath + "/" + jobID);
		
		//Make data directory for this JobID
		if (!FileOperator.makeDir(dir)) {
			return Message.MkDirError;
		}
		//Make result directory for this JobID
		if (!FileOperator.makeDir(new File(master.Parameters.masterResultPath + "/" + jobID))) {
			return Message.MkDirError;
		}
		//Copy marsOut to this JobID
		if (!FileOperator.cpFile(new File(master.Parameters.marsOutLocation), new File(master.Parameters.masterResultPath + "/" + jobID + "/" + "marsOut"))) {
			return Message.CopyFileError;
		}
		//Copy marsOut control file
		if (!FileOperator.cpFile(new File(master.Parameters.marsOutCtlLocation), new File(master.Parameters.masterResultPath + "/" + jobID + "/" + "mars-out.ctl"))) {
			return Message.CopyFileError;
		}
		
		/**********************************************
		 * NEED TO EDIT THE mars-out.ctl              *
		 *********************************************/
		
		
		//Store the file
		byte[] bytes = client.uploadData();
		if (bytes == null) {
			System.err.println("No file uploaded!");
			return Message.UploadError;
		}
		String filePath = master.Parameters.masterDataPath + "/" + jobID + "/" + common.Parameters.dataFileName;
		if (!FileOperator.storeFile(filePath, bytes)) {
			return Message.StoreFileError;
		}
		System.out.println("Job " + jobID + " started!");
		JobTracker.addJob(jobID, job);   //Add the job to job tracker
		job.start();   //Everything goes well, start the job

		System.out.println("Here!");
		
		
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
		String filePath = master.Parameters.masterResultPath + "/" + common.Parameters.resultFileName;
		return FileOperator.getBytes(filePath);
	}
	

}
