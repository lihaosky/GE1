package slave;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import common.FileOperator;
import common.Message;
import common.Parameters;
import master.JobAssigner;

/**
 * This class handle request from master to initiate assignment and upload result
 * @author lihao
 *
 */
public class AssignmentHandlerImp extends UnicastRemoteObject implements AssignmentHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected AssignmentHandlerImp() throws RemoteException {
		super();
	}

	/**
	 * Download data from master, get the replication this slave need to do
	 */
	public int addAssignment(int nodeID, long jobID, ArrayList<Integer> repList, JobAssigner jobAssigner)
			throws RemoteException {
		/*
		 * Store the file from master
		 */
		byte[] bytes = jobAssigner.uploadData(jobID);
		if (bytes == null) {
			System.err.println("No file uploaded from master!");
			return Message.noMasterFileUploaded;
		}
		BufferedOutputStream output;
		try {
			File file = new File(Parameters.slaveDataPath + "/" + jobID);
			FileOperator.makeDir(file);
			output = new BufferedOutputStream(new FileOutputStream(Parameters.slaveDataPath + "/" + jobID + "/" + Parameters.dataFileName));
			output.write(bytes,0,bytes.length);
			output.flush();
			output.close();
		} catch (IOException e) {
			System.err.println("Master file write error!");
			e.printStackTrace();
			return Message.storeMasterFileFail;
		}

		//Start the assignment
		Assignment assignment = new Assignment(nodeID, jobID, repList, jobAssigner);
		assignment.start();
		AssignmentTracker.addAssignment(jobID, assignment);
		
		return Message.OK;
	}

	/**
	 * Upload result to master
	 */
	public byte[] uploadResult(long jobID, int repNum)
			throws RemoteException {
        File file = new File(Parameters.slaveDataPath + "/" + jobID + "/" + repNum + "/" + Parameters.resultFileName);
        byte buffer[] = new byte[(int)file.length()];
        
        BufferedInputStream input;
		try {
			input = new BufferedInputStream(new FileInputStream(Parameters.slaveDataPath + "/" + jobID + "/" + repNum + "/" + Parameters.resultFileName));
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

	/**
	 * Add new replications in case of other's failure
	 */
	public void addRep(ArrayList<Integer> repList) throws RemoteException {
		
		
	}
	

}
