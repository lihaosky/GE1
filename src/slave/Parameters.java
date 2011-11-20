package slave;

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
			FileInputStream fi = new FileInputStream(homeDir + "/" + ".GEslave.config");
			prop = new Properties();
			prop.load(fi);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Directory to store client data in slave
	 */
	public static String slaveDataPath = prop.getProperty("slaveDataPath");
	/**
	 * Path of marsMain
	 */
	public static String marsMainLocation = prop.getProperty("marsMainLocation");
	/**
	 * Marsmain control file location
	 */
	public static String marsMainCtlLocation = prop.getProperty("marsMainCtlLocation");
}
