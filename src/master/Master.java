package master;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import common.FileOperator;

/**
 * Starts the master
 * @author lihao
 *
 */
public class Master {
	/**
	 * Check directory
	 */
	public boolean checkDirAndFile() {
		File file = new File(master.Parameters.masterDataPath);
		FileOperator.makeDir(file);
		file = new File(master.Parameters.masterResultPath);
		FileOperator.makeDir(file);
		file = new File(master.Parameters.marsOutLocation);
		if (!file.exists()) {
			System.out.println("MarsOut doesn't exist!");
			return false;
		}
		file = new File(master.Parameters.marsOutCtlLocation);
		if (!file.exists()) {
			System.out.println("MarsOut control file doesn't exist!");
			return false;
		}
		return true;
		
	}
	
	/**
	 * Start master
	 */
	public void start() {
		
		//Listen on port 1234 for client connection
		try {
			ServerSocket ss = new ServerSocket(common.Parameters.serverPort);
			while (true) {
				Socket s = ss.accept();
				System.out.println("Got connection from client!");
				Thread.sleep(2000);
				//Pass the client socket to job handler to handle
				JobHandler jobHandler = new JobHandler(s);
				jobHandler.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			Class.forName("master.Parameters");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Master master = new Master();
		if (!master.checkDirAndFile()) {
			return;
		}
		master.start();
	}
}
