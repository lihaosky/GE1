package common;

import java.io.File;

public class Directory {
	public static boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }
	    return dir.delete();
	}
	
	public static boolean makeDir(File dir) {
		if(dir.exists())
		{
			deleteDir(dir);
		}
		if (!dir.mkdir()) {
			System.out.println("Fail to make directory in server!");
		}
		return true;
	}
}
