package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.FileOperator;
import common.Message;
import common.Parameters;

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
	public int downloadResult() throws RemoteException {
		try {
			/*
			 * Store the file
			 */
			byte[] bytes = jobHandler.uploadResult();
			if (bytes == null) {
				System.err.println("No file downloaded!");
				return Message.noFileUploaded;
			}
			
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(this.outputFilePath + "/" + Parameters.resultFileName));
			output.write(bytes,0,bytes.length);
			output.flush();
			output.close();
			
			isJobDone.notify(); //Notify that job is done
		} catch (IOException e) {
			System.err.println("Fail to store result file!");
			e.printStackTrace();
			return Message.storeClientFileFail;
		}
		
		return Message.OK;
		
	}
	
	public void setJobHandler(JobHandler jobHandler) {
		this.jobHandler = jobHandler;
	}
	
	public boolean addJob() {
		try {
			jobID = jobHandler.addJob(repNum, me, time);
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
		String filePath = ""; //File path
		String outputFilePath = "";
		if (args.length != 4) {
			System.out.println("Usage: java ClientImp [-DfileDirectory | -Pfilepath] [-OoutputDirectory] ReplicationNumer Time");
			return;
		} else {
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
					System.out.println(outFile.getAbsolutePath());
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
				filePath = file.getAbsolutePath() + "/" + Parameters.dataFileName;
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
		
		ClientImp client;
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		try {
			Registry registry = LocateRegistry.getRegistry("localhost");
			JobHandler jobHandler = (JobHandler)registry.lookup(Parameters.jobHandlerName);
			client = new ClientImp(repNum, filePath, outputFilePath, time);
			client.setJobHandler(jobHandler);
			if (!client.addJob()) {
				System.err.println("Add job failure!");
				return;
			}
			
			ReadInput ri = new ReadInput(client);
			ri.start();
			
			isJobDone.wait();
			
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
	
	public void start() {
		System.out.println("To check the job status, type \"status\"");
		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				String i = br.readLine();
				if (i.equals("status")) {
					client.checkStatus();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
