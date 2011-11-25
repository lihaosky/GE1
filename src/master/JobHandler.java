package master;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import common.CheckStatusAck;
import common.CheckStatusCommand;
import common.Command;
import common.DownloadAck;
import common.FileOperator;
import common.InitJobAck;
import common.InitJobCommand;
import common.Message;

/**
 * Handle client job request
 * @author lihao
 *
 */
public class JobHandler extends Thread {
	private Socket clientSocket;
	
	public JobHandler(Socket s) {
		super();
		clientSocket = s;
	}
	
	public void run() {
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			while (true) {
				//Read client command
				Command cmd = (Command)ois.readObject();
				
				//Initiate job request
				if (cmd.commandID == Command.InitJobCommand) {
					InitJobCommand ijc = (InitJobCommand)cmd;
					int repNum = ijc.repNum;
					int time = ijc.time;
					Long fileLength = ijc.fileLength;
					//System.out.println("Rep number is " + repNum);
					//Thread.sleep(2000);
					long jobID = addJob(repNum, time, fileLength);
					InitJobAck ija = new InitJobAck(Command.InitJobAck, jobID);
					oos.writeObject(ija);
					oos.flush();
					if (jobID < 0) {
						clientSocket.close();
						return;
					}
				}
				//Client check status
				else if (cmd.commandID == Command.CheckStatusCommand) {
					CheckStatusCommand csc = (CheckStatusCommand)cmd;
					Job job = JobTracker.getJob(csc.jobID);
					int finishedRep = job.getFinishedRep();
					CheckStatusAck csa = new CheckStatusAck(Command.CheckStatusAck, finishedRep);
					oos.writeObject(csa);
					oos.flush();
				}
				//Ack to download
				else if (cmd.commandID == Command.DownloadAck) {
					DownloadAck da = (DownloadAck)cmd;
					if (da.status < 0) {
						System.out.println("Client download result error!");
					} else {
						ois.close();
						oos.close();
						clientSocket.close();
						return;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Client add job
	 * @param repNum Number of replications
	 * @param client Remote client object. For calling download
	 * @param time Time limit provide by client
	 */
	public long addJob(int repNum, int time, long fileLength) {
		System.out.println("Replication number is " + repNum);
		System.out.println("Time is " + time);
		System.out.println("File length is " + fileLength);
		
		Job job = new Job(repNum, clientSocket, time);    //Create a new job
		long jobID = job.getJobID();
	
		//Make directory according to the unique jobID
		File dir = new File(master.Parameters.masterDataPath + "/" + jobID);
		
		//Make data directory for this JobID
		System.out.println("Making directory for job " + jobID + "...");
		if (!FileOperator.makeDir(dir)) {
			return Message.MkDirError;
		}
		//Make result directory for this JobID
		System.out.println("Making directory to store result for job " + jobID + "...");
		if (!FileOperator.makeDir(new File(master.Parameters.masterResultPath + "/" + jobID))) {
			return Message.MkDirError;
		}
		//Copy marsOut to this JobID
		System.out.println("Copying marsOut to result directory...");
		if (!FileOperator.cpFile(new File(master.Parameters.marsOutLocation), new File(master.Parameters.masterResultPath + "/" + jobID + "/" + "marsOut"))) {
			return Message.CopyFileError;
		}
		//Copy marsOut control file
		System.out.println("Copying mars-out.ctl to result directory...");
		if (!FileOperator.cpFile(new File(master.Parameters.marsOutCtlLocation), new File(master.Parameters.masterResultPath + "/" + jobID + "/" + "mars-out.ctl"))) {
			return Message.CopyFileError;
		}
		
		System.out.println("Downloading input file from client...");
		String filePath = master.Parameters.masterDataPath + "/" + jobID + "/" + common.Parameters.dataFileName;
		if (!FileOperator.storeFile(clientSocket, filePath, fileLength)) {
			System.out.println("Download client file error!");
			return Message.DownloadError;
		}
		
		/**********************************************
		 * NEED TO EDIT THE mars-out.ctl              *
		 *********************************************/
		
		JobTracker.addJob(jobID, job);   //Add the job to job tracker
		job.start();   //Everything goes well, start the job
		System.out.println("Job " + jobID + " started!");
		
		
		return job.getJobID();
	}
}
