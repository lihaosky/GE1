package master;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Store the parameters for master
 * Maybe should be separated...
 * Maybe get these from configuration file...
 * @author lihao
 *
 */
public class Parameters {
	public static Properties prop;
	static {
		Properties p = System.getProperties();
		String homeDir = p.getProperty("user.home");
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
}
