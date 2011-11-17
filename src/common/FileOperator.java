package common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Zip and unzip file. Also check input data
 * @author lihao
 *
 */
public class FileOperator {

	public static boolean checkInput(File file) {
		//Input is directory
		if (file.isDirectory()) {
			String[] files = file.list();
			for (int i = 0; i < Parameters.neededInputFiles.length; i++) {
				int j = 0; 
				for (j = 0; j < files.length; j++) {
					if (files[j].equals(Parameters.neededInputFiles[i])) {
						break;
					}
				}
				if (j == files.length) {
					System.out.println("You need input file: " + Parameters.neededInputFiles[i] + "!");
					return false;
				}
			}
		} else {   //Input is a zip file
			try {
				ArrayList<String> entryList = new ArrayList<String>();
				ZipFile zf = new ZipFile(file);
				Enumeration<? extends ZipEntry> e = zf.entries();
				while (e.hasMoreElements()) {
					ZipEntry entry = e.nextElement();
					entryList.add(entry.getName());
				}
				for (int i = 0; i < Parameters.neededInputFiles.length; i++) {
					if (!entryList.contains(Parameters.neededInputFiles[i])) {
						System.out.println("You need input file: " + Parameters.neededInputFiles[i] + " in your zip file!");
						return false;
					}
				}
			} catch (ZipException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * Zip input file to data.zip
	 * @param file This should be a directory
	 * @return
	 */
	public static boolean zipFile(File file) {
		int BUFFER = 2048;
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(file.getAbsolutePath() + "/" + Parameters.dataFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            // get a list of files from current directory

            for (int i=0; i < Parameters.neededInputFiles.length; i++) {
        	  String filePath = file.getAbsolutePath() + "/" + Parameters.neededInputFiles[i];
              System.out.println("Adding: " + Parameters.neededInputFiles[i]);
              FileInputStream fi = new FileInputStream(filePath);
              origin = new BufferedInputStream(fi, BUFFER);
              ZipEntry entry = new ZipEntry(Parameters.neededInputFiles[i]);
              out.putNextEntry(entry);
              int count;
              while((count = origin.read(data, 0, BUFFER)) != -1) {
                 out.write(data, 0, count);
              }
              origin.close();
           }
           out.close();
        } catch(Exception e) {
        	System.err.println("Error when zipping file!");
            e.printStackTrace();
            return false;
        }
		return true;
	}
	
	/**
	 * Unzip file to output directory
	 * @param file      File to be unzipped
	 * @param outputDir Output directory
	 * @return
	 */
	public static boolean unzipFile(File file, String outputDir) {
		int BUFFER = 2048;
		try {
	         BufferedOutputStream dest = null;
	         BufferedInputStream is = null;
	         ZipEntry entry;
	         ZipFile zipfile = new ZipFile(file);
	         Enumeration<? extends ZipEntry> e = zipfile.entries();
	         while(e.hasMoreElements()) {
	            entry = (ZipEntry) e.nextElement();
	            System.out.println("Extracting: " +entry);
	            is = new BufferedInputStream(zipfile.getInputStream(entry));
	            int count;
	            byte data[] = new byte[BUFFER];
	            FileOutputStream fos = new FileOutputStream(outputDir + "/" + entry.getName());
	            dest = new BufferedOutputStream(fos, BUFFER);
	            while ((count = is.read(data, 0, BUFFER)) != -1) {
	               dest.write(data, 0, count);
	            }
	            dest.flush();
	            dest.close();
	            is.close();
	         }
	      } catch(Exception e) {
	         e.printStackTrace();
	      }
		return true;
	}
	
	/**
	 * Delete a directory and its content
	 * @param dir Directory name
	 * @return
	 */
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
	
	/**
	 * Make a directory, if it doesn't exist
	 * @param dir Directory name
	 * @return
	 */
	public static boolean makeDir(File dir) {
		if(dir.exists())
		{
			return true;
		}
		if (!dir.mkdir()) {
			System.out.println("Fail to make directory!");
		}
		return true;
	}
}
