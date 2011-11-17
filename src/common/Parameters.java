package common;

public class Parameters {
	/**
	 * Directory to store client data in master
	 */
	public static String masterDataPath = ".";    
	/**
	 * Directory to store client data in slave
	 */
	public static String slaveDataPath = "/home/lihao/Desktop/slavedata";
	/**
	 * Directory to store slave result in master
	 */
	public static String masterResultPath = "/home/lihao/Desktop/masterresult";
	/**
	 * Directory to store final result in client
	 */
	public static String clientResultPath = "/home/lihao/Desktop/clientresult";
	//public static String slaveResultPath = "/home/lihao/Desktop/slaveresult";
	
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
}
