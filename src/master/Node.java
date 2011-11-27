package master;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import common.FileOperator;
import common.Message;
import common.command.AddRepCommand;
import common.command.Command;
import common.command.DownloadAck;
import common.command.DownloadRepCommand;
import common.command.InitAssignmentCommand;
import common.command.InitJobAck;

/**
 * Contains node information
 * @author lihao
 *
 */
public class Node extends Thread {
	private static int nextNodeID = 1;
	public static int DEAD = 0;
	public static int AVAILABLE = 1;
	public static int BUSY = 2;
	
	private int nodeID;
	private long jobID;
	private String IPAddress;
	private Socket slaveSocket;
	private ArrayList<Integer> repList;
	private int status;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private Socket HBsocket;
	private ObjectOutputStream HBoos;
	private ObjectInputStream HBois;
	
	/**
	 * @param IPAddress Node IP address
	 * @param status    Node status: Available, Dead or Busy
	 */
	public Node(String IPAddress, int status) {
		nodeID = getNextNodeID();
		this.IPAddress = IPAddress;
		this.status = status;
		repList = new ArrayList<Integer>();
	}
	
	/**
	 * Find slave handler
	 * @return slave handler
	 */
	public Socket connect() {
		try {
			slaveSocket = new Socket(this.IPAddress, common.Parameters.slavePort);
			System.out.println("Connected to slave!");
			return slaveSocket;
		} catch (UnknownHostException e) {
			System.err.println("Can't find slave!");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Set replication list to slave
	 * @param repList Replication list
	 */
	synchronized public void setReplist(ArrayList<Integer> repList) {
		this.repList = repList;
	}
	
	/**
	 * Set the status of node
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	/**
	 * Add assignment
	 * @param jobID JobID
	 * @param repList Replication list
	 * @param jobAssigner Job Assigner
	 */
	public int addAssignment(long jobID, ArrayList<Integer> repList) {
		try {
			HBsocket = new Socket(this.IPAddress, common.Parameters.HBport);
			HBoos = new ObjectOutputStream(HBsocket.getOutputStream());
			HBois = new ObjectInputStream(HBsocket.getInputStream());
		} catch (UnknownHostException e2) {
			System.err.println("Error connecting to heartbeat!");
			try {
				slaveSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			e2.printStackTrace();
			return Message.UploadError;
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		this.jobID = jobID;
		this.repList = repList;
		File file = new File(FileOperator.masterDataPath(jobID));
		long fileLength = file.length();
		InitAssignmentCommand iac = new InitAssignmentCommand(Command.InitAssignmentCommand, jobID, nodeID, fileLength, repList);
		try {
			oos = new ObjectOutputStream(slaveSocket.getOutputStream());
			oos.writeObject(iac);
			oos.flush();
			//oos.close();
			
			if (!FileOperator.uploadFile(slaveSocket, file.getAbsolutePath(), fileLength)) {
				System.out.println("Upload file error!");
				slaveSocket.close();
				return Message.UploadError;
			}
			
			ois = new ObjectInputStream(slaveSocket.getInputStream());
			Command cmd = (Command)ois.readObject();
			//ois.close();
			InitJobAck ija = (InitJobAck)cmd;
			if (ija.jobID < 0) {
				System.out.println("Add asignment error in slave!");
				slaveSocket.close();
				return Message.UploadError;
			}
			System.out.println("Add assignemnt successfully!");
		} catch (IOException e) {
			System.out.println("IO error!");
			try {
				slaveSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return Message.UploadError;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return Message.OK;
	}
	
	public void run() {
		try {
			while (true) {
					Command cmd = (Command)ois.readObject();
					//ois.close();
					
					//Download result from slave
					if (cmd.commandID == Command.DownloadRepCommand) {
						DownloadRepCommand drc = (DownloadRepCommand)cmd;
						int rep = drc.repNum;
						long fileLength = drc.fileLength;
						FileOperator.makeDir(new File(master.Parameters.masterResultPath + "/" + jobID));
						File file = new File(FileOperator.masterRepPath(jobID, rep));
						FileOperator.makeDir(file);
						String filePath = FileOperator.masterResultPath(jobID, rep);
						System.out.println("Downloading replication " + rep + " from node " + nodeID + " ...");
						
						//Regard this node as failed node, heartbeat will reallocate this node's replication
						if (!FileOperator.storeFile(slaveSocket, filePath, fileLength)) {
							System.out.println("Download replication error!");
							oos.writeObject(new DownloadAck(Command.DownloadAck, -1));
							oos.flush();
							oos.close();
							slaveSocket.close();
							HBoos.close();
							HBois.close();
							HBsocket.close();
							return;
						} else {
							System.out.println("Download replication success!");
							if (!FileOperator.unzipFile(new File(FileOperator.masterResultPath(jobID, rep)), FileOperator.masterRepPath(jobID, rep))) {
								System.out.println("Unzip replication " + rep + " error!");
							}
							oos.writeObject(new DownloadAck(Command.DownloadAck, 1));
							oos.flush();
							Job job = JobTracker.getJob(jobID);
							job.updateRepList();
							removeRep(rep);
						}
					}
					if (cmd.commandID == Command.FinishedAck) {
						oos.close();
						ois.close();
						slaveSocket.close();
						HBoos.close();
						HBois.close();
						HBsocket.close();
					}
			}
		} catch (SocketException e) {
			System.out.println("Close socket to slave " + this.IPAddress);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean heartBeat() {
		try {
			HBoos.writeObject(new Command(Command.PingCommand));
			Command cmd = (Command)HBois.readObject();
			if (cmd.commandID == Command.PingAck) {
				return true;
			} else {
				System.out.println("Ping " + this.IPAddress + " error!");
				return false;
			}
		} catch (IOException e) {
			System.out.println("Ping " + this.IPAddress + " error!");
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Get nodeID
	 * @return NodeID
	 */
	public int getNodeID() {
		return nodeID;
	}
	
	/**
	 * Remove a replication from this node when it is done
	 * @param repNum
	 */
	synchronized public void removeRep(int repNum) {
		for (int i = 0; i < repList.size(); i++) {
			if (repList.get(i) == repNum) {
				repList.remove(i);
			}
		}
	}
	
	/**
	 * Add replication to this node
	 * @param repNum
	 */
	synchronized public void addRep(ArrayList<Integer> addedRep) {
		for (int i = 0; i < addedRep.size(); i++) {
			repList.add(addedRep.get(i));
		}
		try {
			oos.writeObject(new AddRepCommand(Command.AddRepCommand, addedRep));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	synchronized ArrayList<Integer> getRep() {
		return repList;
	}
	
	/**
	 * Clear the replication list of this node
	 */
	synchronized public void clearRep() {
		repList = new ArrayList<Integer>();
	}
	
	/**
	 * Check if there is still replication pending in this node
	 * @return
	 */
	synchronized public boolean isEmptyRep() {
		return repList.size() == 0 ? true : false;
	}
	
	/**
	 * Get IP address of this node
	 * @return
	 */
	public String getIPAddr() {
		return IPAddress;
	}
	
	/**
	 * Get status of node
	 * @return
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * Get next nodeID, this needs to be atomic
	 * @return
	 */
	synchronized static int getNextNodeID() {
		return nextNodeID++;
	}
	
	/**
	 * In case of node failure, close all sockets
	 */
	public void halt() {
		try {
			slaveSocket.close();
			HBsocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void finish() {
		try {
			oos.writeObject(new Command(Command.FinishedCommand));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
