package master;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Store the parameters for master
 * Get these from configuration file...
 * @author lihao
 *
 */
public class Parameters {
	public static Properties prop;
	public static String homeDir;
	static {
		Properties p = System.getProperties();
		homeDir = p.getProperty("user.home");
		try {
			FileInputStream fi = new FileInputStream(homeDir + "/" + ".GEmaster.config");
			prop = new Properties();
			prop.load(fi);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Directory to store client data in master
	 */
	public static String masterDataPath = prop.getProperty("masterDataPath");    
	/**
	 * Directory to store slave result in master
	 */
	public static String masterResultPath = prop.getProperty("masterResultPath");
	/**
	 * Path of marOut
	 */
	public static String marsOutLocation = prop.getProperty("marsOutLocation");
	/**
	 * Marsout control file location
	 */
	public static String marsOutCtlLocation = prop.getProperty("marsOutCtlLocation");
	/**
	 * Node list file
	 */
	public static String nodeListFile = homeDir + "/" + ".nodeList.config";
}
