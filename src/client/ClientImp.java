package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.FileOperator;
import common.Message;
import master.JobHandler;

public class ClientImp extends UnicastRemoteObject implements Client{
	
	private Client me = this;       //This client. Passed to server for callback
	private String filePath;        //File path
	private String outputFilePath;  //Output file path
	private long jobID;              //JobID got from server
	private JobHandler jobHandler;  //JobHandler got from server
	private int repNum;             //Replication number
	private int time;               //Time needed
	public static Object isJobDone = new Object();      //Used to wait for job to be done
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param repNum Replication number
	 * @param filePath File path
	 * @param outputFilePath Output file path
	 * @param time Time
	 * @throws RemoteException
	 */
	protected ClientImp(int repNum, String filePath, String outputFilePath, int time) throws RemoteException {
		super();
		this.outputFilePath = outputFilePath;
		this.repNum = repNum;
		this.filePath = filePath;
		this.time = time;
		isJobDone = false;
		
	}
	
	/**
	 * Upload data to server
	 * @param fileName fileName
	 */
	public byte[] uploadData() throws RemoteException{
        return FileOperator.getBytes(filePath);
	}
	
	/**
	 * Download result from master
	 */
	public int downloadResult() throws RemoteException {
		/*
		 * Store the file
		 */
		/*byte[] bytes = jobHandler.uploadResult();
		if (bytes == null) {
			System.err.println("No file downloaded!");
			return Message.DownloadError;
		}
		String filep = this.outputFilePath + "/" + Parameters.resultFileName;
		if (!FileOperator.storeFile(filep, bytes)) {
			return Message.StoreFileError;
		}*/
		System.out.println("Job is done");
		synchronized(isJobDone) {
			isJobDone.notify();
		}
		return Message.OK;
		
	}
	
	/**
	 * Set job handler
	 * @param jobHandler JobHandler
	 */
	public void setJobHandler(JobHandler jobHandler) {
		this.jobHandler = jobHandler;
	}
	
	/**
	 * Add job
	 * @return
	 */
	public boolean addJob() {
		try {
			jobID = jobHandler.addJob(repNum, me, time);
		} catch (RemoteException e) {
			System.err.println("Remote exception!");
			e.printStackTrace();
		}
		if (jobID == Message.MkDirError || jobID == Message.UploadError || jobID == Message.StoreFileError) {
			System.err.println("Add job fail!");
			return false;
		} 
		System.out.println("Job added! Your jobID is " + jobID);
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
		try {
			Class.forName("client.Parameters");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		//Get input directory and number of replications
		//Lookup server binded JobHandler obj
		//jobHandler.addJob(repNum, fileName) to upload data and start job
		//master will call downloadResult if finished
		String filePath = ""; //File path
		String outputFilePath = "";
		if (args.length != 4) {
			System.out.println("Usage: java ClientImp [-DfileDirectory | -Pfilepath] [-OoutputDirectory] ReplicationNumer Time");
			return;
		} else {
			if (args[0].length() < 3 || args[1].length() < 3) {
				System.out.println("Usage: java ClientImp [-DfileDirectory | -Pfilepath] [-OoutputDirectory] ReplicationNumer Time");
				return;
			}
			if (!args[0].substring(0, 2).equals("-D") && !args[0].substring(0,  2).equals("-P")) {
				System.out.println("Usage: java ClientImp [-DfileDirectory | -Pfilepath] [-OoutputDirectory] ReplicationNumer Time");
				return;
			}
			//Input is a directory
			if (args[0].substring(0, 2).equals("-D")) {
				String directory = args[0].substring(2);
				File file = new File(directory);
				if (!file.exists()) {
					System.out.println("Input file directory doesn't exist!");
					return;
				} 
				if (!file.isDirectory()) {
					System.out.println("Input file directory is not a directory!");
					return;
				}
				if (!args[1].substring(0, 2).equals("-O")) {
					System.out.println("Usage: java ClientImp [-DfileDirectory | -Pfilepath] [-OoutputDirectory] ReplicationNumer Time");
					return;
				} else {
					File outFile = new File(args[1].substring(2));
					if (!outFile.exists()) {
						System.out.println("Output file directory doesn't exist!");
						return;
					}
					if (!outFile.isDirectory()) {
						System.out.println("Output file directory is not a directory!");
						return;
					}
					outputFilePath = outFile.getAbsolutePath();
				}
				
				//Missing some input file
				if (!FileOperator.checkInput(file)) {
					return;
				}
				
				if (!FileOperator.zipFile(file)) {
					return;
				}
				filePath = file.getAbsolutePath() + "/" + common.Parameters.dataFileName;
			} else { //Input is a zip file
				String zipFile = args[0].substring(2);
				File file = new File(zipFile);
				if (!file.exists()) {
					System.out.println("Input file doesn't exist!");
					return;
				}
				if (!zipFile.endsWith(".zip")) {
					System.out.println("Input file should be zipped!");
					return;
				}
				if (!args[1].substring(0, 2).equals("-O")) {
					System.out.println("Usage: java ClientImp [-DfileDirectory | -Pfilepath] [-OoutputDirectory] ReplicationNumer Time");
					return;
				} else {
					File outFile = new File(args[1].substring(2));
					if (!outFile.exists()) {
						System.out.println("Output file directory doesn't exist!");
						return;
					}
					if (!outFile.isDirectory()) {
						System.out.println("Output file directory is not a directory!");
						return;
					}
					outputFilePath = outFile.getAbsolutePath();
				}
				if (!FileOperator.checkInput(file)) {
					return;
				}
				filePath = file.getAbsolutePath();
			}
		}

		int repNum;
		int time;
		try {
			repNum = Integer.parseInt(args[2]);
			time = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			System.err.println("Please provide valid number!");
			return;
		}
		
		ClientImp clientObj;
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		try {
			System.out.println("Master host is " + client.Parameters.masterHost);
			Registry registry = LocateRegistry.getRegistry(client.Parameters.masterHost);
			System.out.println("Here1");
			System.out.println(System.getProperty("java.rmi.server.hostname"));
			JobHandler jobHandler = (JobHandler)registry.lookup(common.Parameters.jobHandlerName);
			System.out.println("Here");

			clientObj = new ClientImp(repNum, filePath, outputFilePath, time);
			clientObj.setJobHandler(jobHandler);
			if (!clientObj.addJob()) {
				System.err.println("Add job failure!");
				return;
			}
			
			ReadInput ri = new ReadInput(clientObj);
			ri.start();
			synchronized(isJobDone) {
				isJobDone.wait();
			}
			System.out.println("Job done!");
			System.exit(0);
			
		} catch (Exception e) {
			System.err.println("Exception when lookup jobhandler");
			e.printStackTrace();
		}
		
		
	}



}

/**
 * Thread to read input from client
 * @author lihao
 *
 */
class ReadInput extends Thread {
	private ClientImp client;
	
	public ReadInput(ClientImp client) {
		this.client = client;
	}
	
	public void run() {
		System.out.println("To check the job status, type \"status\"");
		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				String i = br.readLine();
				if (i.equals("status")) {
					client.checkStatus();
				} else {
					System.out.println("Unknown command! Please input again!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
