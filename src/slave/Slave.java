package slave;

import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.FileOperator;
import common.Parameters;

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
		File file = new File(Parameters.slaveDataPath);
		FileOperator.makeDir(file);
		file = new File(Parameters.marsMainLocation);
		if (!file.exists()) {
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
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(Parameters.slaveHandlerName, assign);
			Assignment.setAssignmentHandler(assign);
			System.out.println("AssignmentHandler bound!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Slave slave = new Slave();
		if (!slave.checkDirAndFile()) {
			return;
		}
		slave.start();
	}
}
