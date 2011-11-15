package master;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.Parameters;

/**
 * Starts the master
 * @author lihao
 *
 */
public class Master {
	public static void main(String[] args) {
		/*
		 * Bind jobHandler, create JobAssigner and assign it to Job
		 * NodeManager find all available nodes
		 * */
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			/*
			 * Create and bind jobhandler
			 */
			//Runtime.getRuntime().exec("rmiregistyr &");
			String jobHandlerName = Parameters.jobHandlerName;
			JobHandler jobHandler = new JobHandlerImp();
			//JobHandler jobHandlerStub = (JobHandler) UnicastRemoteObject.exportObject(jobHandler, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(jobHandlerName, jobHandler);
			System.out.println("JobHandler bound");
			
			JobAssigner jobAssigner = new JobAssignerImp();
			Job.setJobAssigner(jobAssigner);
		} catch (Exception e) {
			System.err.println("JobHandler bind exception!");
			e.printStackTrace();
		}
	}
}
