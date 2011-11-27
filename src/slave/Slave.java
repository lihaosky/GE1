package slave;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import master.Parameters;
import common.FileOperator;

/**
 * Starts the slave
 * @author lihao
 *
 */
public class Slave {
	/**
	 * Check directory
	 */
	public boolean checkDirAndFile() {
		File file = new File(slave.Parameters.slaveDataPath);
		FileOperator.makeDir(file);
		file = new File(slave.Parameters.marsMainLocation);
		if (!file.exists()) {
			System.out.println("marsMain doesn't exist!");
			return false;
		}
		file = new File(slave.Parameters.marsMainCtlLocation);
		if (!file.exists()) {
			System.out.println("marsMain control file doesn't exist!");
			return false;
		}
		return true;
	}
	
	/**
	 * Start slave
	 */
	public void start() {
		try {
			ServerSocket ss = new ServerSocket(common.Parameters.slavePort);
			while (true) {
				Socket s = ss.accept();
				System.out.println("Got connection from master");
				AssignmentHandler ah = new AssignmentHandler(s);
				ah.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		try {
			Class.forName("slave.Parameters");
			System.out.println("Slave data location is: " + slave.Parameters.slaveDataPath);
			System.out.println("marsMain location is: " + slave.Parameters.marsMainLocation);
			System.out.println("marsMain control file location is: " + slave.Parameters.marsMainCtlLocation);
			System.out.println("Your home directory is: " + Parameters.homeDir);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Slave slave = new Slave();
		if (!slave.checkDirAndFile()) {
			return;
		}
		System.out.println("Heart beat started...");
		Heartbeat hb = new Heartbeat();
		hb.start();
		System.out.println("Slave started...");
		slave.start();
	}
}
