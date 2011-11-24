package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
/**
 * 
 * Store the parameters for client
 * Get these from configuration file...
 * @author lihao
 *
 */
public class Parameters {
	public static Properties prop;
	static {
		Properties p = System.getProperties();
		String homeDir = p.getProperty("user.home");
		try {
			FileInputStream fi = new FileInputStream(homeDir + "/" + ".GEclient.config");
			prop = new Properties();
			prop.load(fi);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Master IP address
	 */
	public static String masterHost = prop.getProperty("masterHost");
}
