package slave;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.Parameters;

/**
 * Starts the slave
 * @author lihao
 *
 */
public class Slave {
	public static void main(String[] args) {
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
}
