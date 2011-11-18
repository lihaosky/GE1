package master;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.FileOperator;
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

	/**
	 * Download result from slave
	 */
	public int downloadResult(int nodeID, long jobID, int repNum, AssignmentHandler assignmentHandler) throws RemoteException {
		
		FileOperator.makeDir(new File(Parameters.masterResultPath + "/" + jobID));
		File file = new File(Parameters.masterResultPath + "/" + jobID + "/" + repNum);
		FileOperator.makeDir(file);
		
		/*
		 * Store the file
		 */
		byte[] bytes = assignmentHandler.uploadResult(jobID, repNum);
		if (bytes == null) {
			System.err.println("No file uploaded!");
			return Message.UploadError;
		}
		String filePath = Parameters.masterResultPath + "/" + jobID + "/" + repNum + "/" + Parameters.resultFileName;
		if (!FileOperator.storeFile(filePath, bytes)) {
			return Message.StoreFileError;
		}
		//TO BE DONE: may need to unzip the file
		if (!FileOperator.unzipFile(new File(filePath), Parameters.masterResultPath + "/" + jobID + "/" + repNum)) {
			return Message.UnzipFileError;
		}
		
		Job job = JobTracker.getJob(jobID);
		job.updateRepList(nodeID, repNum);
		
		if (job.checkNodeStatus()) {
			job.isJobDone.notify();   //Notify that job is done
		}
		return Message.OK;
	}

	/**
	 * Upload file to slave
	 * @param jobID JobID
	 */
	public byte[] uploadData(long jobID) throws RemoteException {
		String filePath = Parameters.masterDataPath + "/" + jobID + "/" + Parameters.dataFileName;
		return FileOperator.getBytes(filePath);
	}

}
