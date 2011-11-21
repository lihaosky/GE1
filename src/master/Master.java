package master;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

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
			String jobHandlerName = common.Parameters.jobHandlerName;
			JobHandler jobHandler = new JobHandlerImp();
			JobHandler jobHandlerStub = (JobHandler)UnicastRemoteObject.exportObject(jobHandler, 1234);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(jobHandlerName, jobHandlerStub);
			System.out.println("JobHandler bound");
			
			JobAssigner jobAssigner = new JobAssignerImp();
			Job.setJobAssigner(jobAssigner);
		} catch (Exception e) {
			System.err.println("JobHandler bind exception!");
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
