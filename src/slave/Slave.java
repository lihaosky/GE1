package slave;

import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

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
		//start assignment handler...
		try {
			AssignmentHandler assign = new AssignmentHandlerImp();
			AssignmentHandler assignStub = (AssignmentHandler)UnicastRemoteObject.exportObject(assign, 1234);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(common.Parameters.slaveHandlerName, assignStub);
			Assignment.setAssignmentHandler(assign);
			System.out.println("AssignmentHandler bound!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			Class.forName("slave.Parameters");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Slave slave = new Slave();
		if (!slave.checkDirAndFile()) {
			return;
		}
		slave.start();
	}
}
