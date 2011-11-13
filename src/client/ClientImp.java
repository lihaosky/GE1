package client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.Message;
import common.Parameters;

import master.JobHandler;

public class ClientImp extends UnicastRemoteObject implements Client{
	
	private Client me = this;       //This client. Passed to server for callback
	private String fileDir;         //File directory
	private String fileName;        //File name
	private String filePath;        //File path
	private int jobID;              //JobID got from server
	private JobHandler jobHandler;  //JobHandler got from server
	private int repNum;             //Replication number
	private int time;               //Time needed
	public boolean isJobDone;      //Is job dnoe
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ClientImp(int repNum, String filePath, String fileName, int time) throws RemoteException {
		super();
		this.repNum = repNum;
		this.filePath = filePath;
		this.time = time;
		this.fileName = fileName;
		isJobDone = false;
		
	}
	
	/**
	 * Upload data to server
	 * @param fileName fileName
	 */
	public byte[] uploadData() throws RemoteException{
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
	
	/**
	 * Download result from master
	 */
	public void downloadResult() throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	public void setJobHandler(JobHandler jobHandler) {
		this.jobHandler = jobHandler;
	}
	
	public boolean addJob() {
		try {
			jobID = jobHandler.addJob(repNum, fileName, me);
		} catch (RemoteException e) {
			System.err.println("Remote exception!");
			e.printStackTrace();
		}
		if (jobID == Message.mkDirFail || jobID == Message.noFileUploaded || jobID == Message.storeClientFileFail) {
			System.err.println("Add job fail!");
			return false;
		} 
		return true;
	}
	
	public void checkStatus() {
		try {
			jobHandler.getJobStatus(jobID);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * args[0] is the file directory
	 * args[1] is the number of replication
	 * args[2] is the desired time
	 * @param args
	 */
	public static void main(String[] args) {
		//Get input directory and number of replications
		//Lookup server binded JobHandler obj
		//jobHandler.addJob(repNum, fileName) to upload data and start job
		//master will call downloadResult if finished
		if (args.length != 3) {
			System.out.println("Usage: java ClientImp Filepath ReplicationNumer Time");
			return;
		}
		File file = new File(args[0]);
	
		if (!file.exists()) {
			System.err.println("File doesn't exist!");
			return;
		}
		String fileName = file.getName();
		String filePath = args[0];
		int repNum;
		int time;
		try {
			repNum = Integer.parseInt(args[1]);
			time = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			System.err.println("Please provide valid number!");
			return;
		}
		
		ClientImp client;
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		try {
			Registry registry = LocateRegistry.getRegistry("localhost");
			JobHandler jobHandler = (JobHandler)registry.lookup(Parameters.jobHandlerName);
			client = new ClientImp(repNum, filePath, fileName, time);
			client.setJobHandler(jobHandler);
			if (!client.addJob()) {
				System.err.println("Add job failure!");
				return;
			}
			
			ReadInput ri = new ReadInput(client);
			ri.start();
			
			while (!client.isJobDone) {
			}
			
			System.out.println("Job done!");
			System.exit(0);
			
		} catch (Exception e) {
			System.err.println("Exception when lookup jobhandler");
			e.printStackTrace();
		}
		
		
	}



}

class ReadInput extends Thread {
	private ClientImp client;
	
	public ReadInput(ClientImp client) {
		this.client = client;
	}
	
	public void start() {
		System.out.println("To check the job status, press 1");
		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				String i = br.readLine();
				if (i.equals("1")) {
					client.checkStatus();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
