package common;

/**
 * Store the parameters
 * Maybe should be separated...
 * Maybe get these from configuration file...
 * @author lihao
 *
 */
public class Parameters {
	/**
	 * Master job handler name for client lookup
	 */
	public static String jobHandlerName = "jobHandler";
	/**
	 * Slave job handler name for master lookup
	 */
	public static String slaveHandlerName = "slaveHandler";
	
	/**
	 * Data file name in all the place
	 */
	public static String dataFileName = "data.zip";
	
	/**
	 * Result file name in client
	 */
	public static String resultFileName = "result.zip";
	/**
	 * Needed input files from client
	 */
	public static String[] neededInputFiles = {"mars.in02", "mars.in05", "mars.in17", "MARS-LIC", "mars.ctl"};
	/**
	 * Server port number
	 */
	public static int serverPort = 1234;
	/**
	 * Slave port number
	 */
	public static int slavePort = 1099;
}
