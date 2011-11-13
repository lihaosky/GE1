package client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import master.JobHandler;

public class ClientImp extends UnicastRemoteObject implements Client{
	
	private Client me = this;       //This client. Passed to server for callback
	private String fileDir;         //File directory
	private String fileName;        //File name
	private int jobId;              //JobID got from server
	private JobHandler jobHandler;  //JobHandler got from server
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ClientImp() throws RemoteException {
		super();
	}
	
	/**
	 * Upload data to server
	 * @param fileName fileName
	 */
	public byte[] uploadData() throws RemoteException{
        File file = new File(fileName);
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
	
	/**
	 * Download result from master
	 */
	public void downloadResult() throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		//Get input directory and number of replications
		//Lookup server binded JobHandler obj
		//jobHandler.addJob(repNum, fileName) to upload data and start job
		//master will call downloadResult if finished
	}



}
