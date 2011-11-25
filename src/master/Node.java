package master;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import common.Command;
import common.DownloadAck;
import common.DownloadRepCommand;
import common.FileOperator;
import common.InitAssignmentCommand;
import common.InitJobAck;
import common.Message;

/**
 * Contains node information
 * @author lihao
 *
 */
public class Node extends Thread {
	private static int nextNodeID = 1;
	private int nodeID;
	private long jobID;
	private String IPAddress;
	private Socket slaveSocket;
	private ArrayList<Integer> repList;
	private int status;
	public static int DEAD = 0;
	public static int AVAILABLE = 1;
	public static int BUSY = 2;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
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
				return Message.UploadError;
			}
			System.out.println("Add assignemnt successfully!");
		} catch (IOException e) {
			System.out.println("IO error!");
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
						if (!FileOperator.storeFile(slaveSocket, filePath, fileLength)) {
							System.out.println("Download replication error!");
							oos.writeObject(new DownloadAck(Command.DownloadAck, -1));
							oos.flush();
							oos.close();
							slaveSocket.close();
							this.status = Node.AVAILABLE;
						} else {
							System.out.println("Download replication success!");
							if (!FileOperator.unzipFile(new File(FileOperator.masterResultPath(jobID, rep)), FileOperator.masterRepPath(jobID, rep))) {
								System.out.println("Unzip replication " + rep + " error!");
							}
							oos.writeObject(new DownloadAck(Command.DownloadAck, 1));
							oos.flush();
							//oos.close();
							Job job = JobTracker.getJob(jobID);
							job.updateRepList(nodeID, rep);
							if (job.checkNodeStatus()) {
								synchronized (job.isJobDone) {
									job.isJobDone.notify();
								}
								this.status = Node.AVAILABLE;
								return;
							}
							removeRep(rep);
							if (this.isEmptyRep()) {
								oos.close();
								slaveSocket.close();
								this.status = Node.AVAILABLE;
								return;
							}
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
	synchronized public void addRep(int repNum) {
		repList.add(repNum);
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
	public boolean isEmptyRep() {
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
}
