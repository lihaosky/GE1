package client;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import common.FileOperator;
import common.command.CheckStatusAck;
import common.command.CheckStatusCommand;
import common.command.Command;
import common.command.DownloadAck;
import common.command.DownloadCommand;
import common.command.ErrorCommand;
import common.command.InitJobAck;
import common.command.InitJobCommand;

public class Client {
	
	private String filePath;        //File path
	private String outputFilePath;  //Output file path
	private int repNum;             //Replication number
	private int time;               //Time needed
	public static Object isJobDone = new Object();      //Used to wait for job to be done

	/**
	 * Constructor
	 * @param repNum Replication number
	 * @param filePath File path
	 * @param outputFilePath Output file path
	 * @param time Time
	 * @throws RemoteException
	 */
	protected Client(int repNum, String filePath, String outputFilePath, int time) {
		super();
		this.outputFilePath = outputFilePath + "/" + common.Parameters.resultFileName;
		this.repNum = repNum;
		this.filePath = filePath;
		this.time = time;
		isJobDone = false;
		
	}
	
	/**
	 * Add job
	 * @return
	 */
	public boolean addJob() {
		Socket s = null;
		try {
			s = new Socket(Parameters.masterHost, common.Parameters.serverPort);
			
			//Initiate job
			File file = new File(this.filePath);
			InitJobCommand ijc = new InitJobCommand(Command.InitJobCommand, repNum, time, file.length());
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(ijc);
			oos.flush();
			//oos.close();
			
			//Upload file
			if (!FileOperator.uploadFile(s, this.filePath, file.length())) {
				System.out.println("Upload file error!");
				s.close();
				return false;
			}
			
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			while (true) {
				Command cmd = (Command)ois.readObject();
				//ois.close();
				
				//Ack to initjob
				if (cmd.commandID == Command.InitJobAck) {
					InitJobAck ija = (InitJobAck)cmd;
					long jobID = ija.jobID;
					if (jobID < 0) {
						System.out.println("Server error!");
						s.close();
						return false;
					} else {
						System.out.println("Job successful added");
						System.out.println("Your jobID is " + jobID);
						ReadInput ri = new ReadInput(jobID, oos);
						ri.start();
					}
				} 
				//Ask client to download result
				if (cmd.commandID == Command.DownloadCommand) {
					DownloadCommand dc = (DownloadCommand)cmd;
					System.out.println("Downloading result from server...");
					if (!FileOperator.storeFile(s, outputFilePath, dc.fileLength)) {
						System.out.println("Error when downloading result!");
						oos = new ObjectOutputStream(s.getOutputStream());
						oos.writeObject(new DownloadAck(Command.DownloadAck, -1));
						oos.flush();
						oos.close();
						ois.close();
						s.close();
						return false;
					} 
					System.out.println("Result downloaded!");
					System.out.println("Result is stored in " + outputFilePath);
					oos.writeObject(new DownloadAck(Command.DownloadAck, 1));
					oos.flush();
					oos.close();
					ois.close();
					s.close();
					return true;
				}
				//Ack to check status
				if (cmd.commandID == Command.CheckStatusAck) {
					CheckStatusAck csa = (CheckStatusAck)cmd;
					System.out.println("Finished " + csa.finishedRep + " replications (" + ((double)csa.finishedRep) / repNum * 100 + "%)");
				}
				//Error message
				if (cmd.commandID == Command.ErrorMessage) {
					ErrorCommand em = (ErrorCommand)cmd;
					System.out.println(em.message);
					oos.writeObject(new Command(Command.ErrorAck));
					oos.flush();
					s.close();
					return false;
				}
			}
		} catch (EOFException e) {
			System.err.println("Server closed socket!");
			try {
				s.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		} catch(SocketException e) {
			System.err.println("Server closed socket!");
			try {
				s.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		} catch (UnknownHostException e) {
			System.err.println("Can't find the server!");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
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
		
		Client clientObj = new Client(repNum, filePath, outputFilePath, time);

		if (!clientObj.addJob()) {
			System.err.println("Server error!");
		}
		
		System.exit(0);
		
		
	}



}

/**
 * Thread to read input from client
 * @author lihao
 *
 */
class ReadInput extends Thread {
	private long jobID;
	private ObjectOutputStream oos;
	
	public ReadInput(long jobID, ObjectOutputStream oos) {
		this.jobID = jobID;
		this.oos = oos;
	}
	
	public void run() {
		System.out.println("To check the job status, type \"status\"");
		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				String i = br.readLine();
				if (i.equals("status")) {
					CheckStatusCommand csc = new CheckStatusCommand(Command.CheckStatusCommand, jobID);
					oos.writeObject(csc);
					oos.flush();
				} else {
					System.out.println("Unknown command! Please input again!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
