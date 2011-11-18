package common;

public class Parameters {
	/**
	 * Directory to store client data in master
	 */
	public static String masterDataPath = "/home/lihao/Desktop/masterdata";    
	/**
	 * Directory to store client data in slave
	 */
	public static String slaveDataPath = "/home/lihao/Desktop/slavedata";
	/**
	 * Directory to store slave result in master
	 */
	public static String masterResultPath = "/home/lihao/Desktop/masterresult";
	
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
	public static String[] neededInputFiles = {"mars.in02", "mars.in05", "mars.in17", "MARS-LIC"};
	/**
	 * Path of marsMain
	 */
	public static String marsMainLocation = ".";
	/**
	 * Path of marOut
	 */
	public static String marsOutLocation = "/home/lihao/Desktop/marsOut";
	/**
	 * Marsout control file location
	 */
	public static String marsOutCtlLocation = "/home/lihao/Desktop/GE_Project/mars-out.ctl";
	/**
	 * Marsmain control file location
	 */
	public static String marsMainCtlLocation = "/home/lihao/Desktop/GE_Project/p1/mars.ctl";
}
