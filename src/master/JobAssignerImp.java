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

import common.Directory;
import common.Message;
import common.Parameters;
import slave.AssignmentHandler;

/**
 * This class upload data to slave and download result
 * @author lihao
 *
 */
public class JobAssignerImp extends UnicastRemoteObject implements JobAssigner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected JobAssignerImp() throws RemoteException {
		super();
	}

	@Override
	public int downloadResult(int nodeID, long jobID, int repNum, AssignmentHandler assignmentHandler) throws RemoteException {
		
		Directory.makeDir(new File(Parameters.masterResultPath + "/" + jobID));
		File file = new File(Parameters.masterResultPath + "/" + jobID + "/" + repNum);
		
		try {
			/*
			 * Store the file
			 */
			byte[] bytes = assignmentHandler.uploadResult(jobID, repNum);
			if (bytes == null) {
				System.err.println("No file uploaded!");
				return Message.noFileUploaded;
			}
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(Parameters.masterResultPath + "/" + jobID + "/" + repNum + "/" + Parameters.resultFileName));
			output.write(bytes,0,bytes.length);
			output.flush();
			output.close();
			
			//TO BE DONE: may need to unzip the file
			
			
		} catch (IOException e) {
			System.err.println("Fail to store slave file!");
			e.printStackTrace();
			return Message.storeSlaveFileFail;
		}
		
		Job job = JobTracker.getJob(jobID);
		job.updateRepList(nodeID, repNum);
		
		if (job.checkNodeStatus()) {
			job.isJobDone.notify();   //Notify that job is done
		}
		return Message.OK;
	}

	@Override
	public byte[] uploadData(long jobID) throws RemoteException {
        File file = new File(Parameters.masterDataPath + "/" + jobID + "/" + Parameters.dataFileName);
        byte buffer[] = new byte[(int)file.length()];
        
        BufferedInputStream input;
		try {
			input = new BufferedInputStream(new FileInputStream(Parameters.masterDataPath + "/" + jobID + "/" + Parameters.dataFileName));
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
