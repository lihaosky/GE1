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
	public int downloadResult(int jobID, String fileName, int repNum,
			AssignmentHandler assignmentHandler) throws RemoteException {
		File file = new File(Parameters.masterResultPath + "/" + jobID + "/" + repNum);
		if (!file.mkdir()) {
			System.err.println("Master make result directory failure!");
			return Message.mkDirFail;
		}
		
		try {
			/*
			 * Store the file
			 */
			byte[] bytes = assignmentHandler.uploadResult(jobID, repNum, fileName);
			if (bytes == null) {
				System.err.println("No file uploaded!");
				return Message.noFileUploaded;
			}
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(Parameters.masterResultPath + "/" + jobID + "/" + repNum + "/" + fileName));
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
		job.updateRepList(repNum);
		
		return Message.OK;
	}

	@Override
	public byte[] uploadData(int jobID, String fileName) throws RemoteException {
        File file = new File(Parameters.clientDataPath + "/" + jobID + "/" + fileName);
        byte buffer[] = new byte[(int)file.length()];
        
        BufferedInputStream input;
		try {
			input = new BufferedInputStream(new FileInputStream(fileName));
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
