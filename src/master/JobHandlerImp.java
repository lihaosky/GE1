package master;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import client.Client;
import common.Directory;
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

	
	public int addJob(int repNum, String fileName, Client client) throws RemoteException {
		Job job = new Job(repNum, fileName, client);    //Create a new job
		int jobID = job.getJobID();
	    
		/*
		 * Make directory according to the unique jobID
		 */
		File dir = new File(Parameters.clientDataPath + "/" + jobID);
		
		Directory.makeDir(dir);
		Directory.makeDir(new File(Parameters.masterResultPath + "/" + jobID));
		
		try {
			/*
			 * Store the file
			 */
			byte[] bytes = client.uploadData();
			if (bytes == null) {
				System.err.println("No file uploaded!");
				return Message.noFileUploaded;
			}
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(Parameters.clientDataPath + "/" + jobID + "/" + fileName));
			output.write(bytes,0,bytes.length);
			output.flush();
			output.close();
			
			//TO BE DONE: may need to unzip the file
			
			
		} catch (IOException e) {
			System.err.println("Fail to store client file!");
			e.printStackTrace();
			return Message.storeClientFileFail;
		}
		
		job.start();   //Everything goes well, start the job
		JobTracker.addJob(jobID, job);   //Add the job to job tracker
		
		return job.getJobID();
	}
	
	public void getJobStatus(int jobId) throws RemoteException {
		
	}
	
	/**
	 * Upload result to client
	 */
	public byte[] uploadResult() throws RemoteException {
		String filePath = Parameters.masterResultPath + "/" + "result.txt";
        File file = new File(filePath);
        byte buffer[] = new byte[(int)file.length()];
        
        BufferedInputStream input;
		try {
			input = new BufferedInputStream(new FileInputStream(filePath));
	        input.read(buffer,0,buffer.length);
	        input.close();
	        return(buffer);
		} catch (FileNotFoundException e) {
			System.err.println("Can't find file to upload!");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.println("Error when read file!");
			e.printStackTrace();
			return null;
		}
	}
	

}
