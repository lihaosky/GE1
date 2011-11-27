package slave;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import common.FileOperator;
import common.Message;
import common.command.AddRepCommand;
import common.command.Command;
import common.command.DownloadAck;
import common.command.InitAssignmentCommand;
import common.command.InitJobAck;

/**
 * This class handle request from master to initiate assignment and upload result
 * @author lihao
 *
 */
public class AssignmentHandler extends Thread {
	private Socket masterSocket;
	public long jobID;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private int repNum;
	private int nodeID;
	
	public AssignmentHandler(Socket s) {
		super();
		masterSocket = s;
	}

	public void run() {
		try {
			ois = new ObjectInputStream(masterSocket.getInputStream());
			oos = new ObjectOutputStream(masterSocket.getOutputStream());
			while (true) {
				Command cmd = (Command)ois.readObject();
	
				//New assignment from master
				if (cmd.commandID == Command.InitAssignmentCommand) {
					InitAssignmentCommand iac = (InitAssignmentCommand)cmd;
					long jobID = iac.jobID;
					this.nodeID = iac.nodeID;
					long fileLength = iac.fileLength;
					ArrayList<Integer> repList = iac.repList;
					int status = addAssignment(nodeID, jobID, fileLength, repList);
					InitJobAck ija = new InitJobAck(Command.InitJobAck, status);
					oos.writeObject(ija);
					oos.flush();
					if (status < 0) {
						masterSocket.close();
						return;
					}
				}
				//Download ack from master
				else if (cmd.commandID == Command.DownloadAck) {
					DownloadAck da = (DownloadAck)cmd;
					if (da.status > 0) {
						repNum--;
						//No more replication to execute
						if (repNum == 0) {
							System.out.println("Master downloaded all the replications!");
							ois.close();
							oos.close();
							masterSocket.close();
							AssignmentTracker.removeAssignment(jobID);
							return;
						}
					}
				}
				//Add more replication from master
				else if (cmd.commandID == Command.AddRepCommand) {
					AddRepCommand arc = (AddRepCommand)cmd;
					Assignment a = AssignmentTracker.getAssignment(jobID);
					repNum += arc.repList.size();
					if (!a.isFinished()) {
						a.addRep(arc.repList);
					} else {
						AssignmentTracker.removeAssignment(jobID);
						Assignment assignment = new Assignment(nodeID, jobID, arc.repList, masterSocket, oos);
						AssignmentTracker.addAssignment(jobID, assignment);
						assignment.start();
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
	 * Download data from master, get the replication this slave need to do
	 */
	public int addAssignment(int nodeID, long jobID, long fileLength, ArrayList<Integer> repList) {
		this.jobID = jobID;
		repNum = repList.size();
		
		//Make data directory
		System.out.println("Making data directory...");
		File file = new File(slave.Parameters.slaveDataPath + "/" + jobID);
		if (!FileOperator.makeDir(file)) {
			return Message.MkDirError;
		}
		
		//Store file
		System.out.println("Downloading file from master...");
		String filePath = FileOperator.slaveDataPath(jobID);
		if (!FileOperator.storeFile(masterSocket, filePath, fileLength)) {
			System.out.println("Download master file error!");
			return Message.DownloadError;
		}
		
		
		//Start the assignment
		Assignment assignment = new Assignment(nodeID, jobID, repList, masterSocket, oos);
		AssignmentTracker.addAssignment(jobID, assignment);
		assignment.start();

		return Message.OK;
	}
}
