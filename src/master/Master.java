package master;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.FileOperator;
import common.Parameters;

/**
 * Starts the master
 * @author lihao
 *
 */
public class Master {
	/**
	 * Check directory
	 */
	public void checkDir() {
		File file = new File(Parameters.masterDataPath);
		FileOperator.makeDir(file);
		file = new File(Parameters.masterResultPath);
		FileOperator.makeDir(file);
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
			String jobHandlerName = Parameters.jobHandlerName;
			JobHandler jobHandler = new JobHandlerImp();
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
	
	public static void main(String[] args) {
		Master master = new Master();
		master.checkDir();
		master.start();
	}
}
