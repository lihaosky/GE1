package common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
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

	/**
	 * Check client input file, should contain data files
	 * @param file File
	 * @return
	 */
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
        	System.err.println("Zip file error!");
            e.printStackTrace();
            return false;
        }
		return true;
	}
	
	/**
	 * Zip all the files in the directory except excludedFiles
	 * @param file File directory
	 * @param excludedFiles Files excluded
	 * @return
	 */
	public static boolean zipExcludeFile(File file, String[] excludedFiles) {
		int BUFFER = 2048;
        try {
        	// get a list of files from current directory
        	String[] fileList = file.list();
        	
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(file.getAbsolutePath() + "/" + Parameters.resultFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            
            for (int i = 0; i < fileList.length; i++) {
            	boolean exist = false;
            	for (int j = 0; j < excludedFiles.length; j++) {
            		if (fileList[i].equals(excludedFiles[j])) {
            			exist = true;
            		}
            	}
            	if (exist) {
            		continue;
            	}
	            String filePath = file.getAbsolutePath() + "/" + fileList[i];
	            File f = new File(filePath);
	            if (f.isDirectory()) {
	            	continue;
	            }
	            System.out.println("Adding: " + fileList[i]);
	            FileInputStream fi = new FileInputStream(filePath);
	            origin = new BufferedInputStream(fi, BUFFER);
	            ZipEntry entry = new ZipEntry(fileList[i]);
	            out.putNextEntry(entry);
	            int count;
	            while((count = origin.read(data, 0, BUFFER)) != -1) {
	            	out.write(data, 0, count);
	            }
	            origin.close();
            }
           out.close();
        } catch(Exception e) {
        	System.err.println("Zip file error!");
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
	           // System.out.println("Extracting: " +entry);
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
	    	  System.err.println("Unzip file error!");
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
			return false;
		}
		return true;
	}
	
	/**
	 * Copy file
	 * @param file1 Original location
	 * @param file2 New location
	 */
	public static boolean cpFile(File file1, File file2) {
		/*
        byte buffer[] = new byte[(int)file1.length()];
        
        BufferedInputStream input;
		try {
			input = new BufferedInputStream(new FileInputStream(file1));
	        input.read(buffer,0,buffer.length);
	        input.close();
	        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file2));
			output.write(buffer,0,buffer.length);
			output.flush();
			output.close();
			return true;
		} catch (FileNotFoundException e) {
			System.err.println(file1.getName() + " doesn't exist!");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println("Error when read file!");
			e.printStackTrace();
			return false;
		}*/
		try {
			Process p = Runtime.getRuntime().exec("cp " + file1.getAbsolutePath() + " " + file2.getAbsolutePath());
			p.waitFor();
			return true;
		} catch (IOException e) {
			System.out.println("Error when copy file!");
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Edit the mars.ctl file: add repNum in the first two lines
	 * @param file
	 * @param repNum
	 * @return
	 */
	public static boolean editMarsCtl(File file, int repNum) {
		try {
			File file1 = new File(file.getAbsolutePath() + "tmp");
			BufferedWriter bw = new BufferedWriter(new FileWriter(file1));
			BufferedReader br = new BufferedReader(new FileReader(file));
			bw.write("" + repNum);
			bw.newLine();
			bw.write("" + repNum);
			bw.newLine();
			String line = br.readLine();
			while (line != null) {
				bw.write(line);
				bw.newLine();
				line = br.readLine();
			}
			file.delete();
			file1.renameTo(file);
			bw.flush();
			bw.close();
			br.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 
	}
	
	/**
	 * Edit mars-out.ctl file
	 * @param file
	 * @param repNum
	 * @return
	 */
	public static boolean editMarsoutCtl(File file, int repNum) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			BufferedReader br = new BufferedReader(new FileReader(master.Parameters.marsOutCtlLocation));
			String line = br.readLine();
			bw.write(line);
			bw.newLine();
			bw.write("1/mars.ot09");
			bw.newLine();
			bw.write("1/results-1.bin");
			bw.newLine();
			for (int i = 2; i <= repNum; i++) {
				bw.write(i + "/results-" + i + ".bin");
				bw.newLine();
			}
			while ((line = br.readLine()) != null) {
				bw.write(line);
				bw.newLine();
			}
			br.close();
			bw.flush();
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Store a file from socket
	 * @param filePath File path
	 * @param bytes Bytes of file
	 * @return
	 */
	public static boolean storeFile(Socket s, String filePath, long fileLength) {
		try {
			byte[] buffer = new byte[1024 * 4];
			BufferedInputStream bis = new BufferedInputStream(s.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
			int totalRead = 0;
			int readCount;
			while ((readCount = bis.read(buffer)) > 0) {
				totalRead += readCount;
				bos.write(buffer, 0, readCount);
				System.out.print("\r" + (int)(((double)totalRead) / fileLength * 100) + "% downloaded...");
				if (totalRead == fileLength) {
					break;
				}
			}
			System.out.println();
			bos.flush();
			bos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
			
		}
	}
	
	/**
	 * Send file over socket
	 * @param s
	 * @param filePath
	 * @return
	 */
	public static boolean uploadFile(Socket s, String filePath, long fileLength) {
		try {
			byte[] buffer = new byte[1024 * 4];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
			BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
			int totalRead = 0;
			int readCount;
			while ((readCount = bis.read(buffer)) > 0) {
				totalRead += readCount;
				bos.write(buffer, 0, readCount);
				System.out.print("\r" + (int)(((double)totalRead) / fileLength * 100) + "% uploaded...");
			}
			System.out.println();
			bos.flush();
			bis.close();
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("Can't find file: " + filePath);
			//e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Delete all files in the directory
	 * @param dir Directory name
	 * @return
	 */
	public static boolean removeAllFiles(File dir) {
		String[] list = dir.list();
		for (int i = 0; i < list.length; i++) {
			File file = new File(dir.getAbsolutePath() + "/" + list[i]);
			if (file.exists()) {
				if (!file.delete()) {
					System.out.println("Can't delete file " + file.getAbsolutePath());
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Get data file path of jobID in slave
	 * @param jobID JobID
	 * @return Slave data file path of jobID
	 */
	public static String slaveDataPath(long jobID) {
		return slave.Parameters.slaveDataPath + "/" + jobID + "/" + Parameters.dataFileName;
	}
	
	/**
	 * Get replication directory path of rep of jobID
	 * @param jobID JobID
	 * @param rep Replication number
	 * @return Slave path of replication number rep
	 */
	public static String slaveRepPath(long jobID, int rep) {
		return slave.Parameters.slaveDataPath + "/" + jobID + "/" + rep;
	}
	
	/**
	 * Get result file path of rep of jobID
	 * @param jobID JobID
	 * @param rep Replication number
	 * @return Slave result file path of replication number rep
	 */
	public static String slaveResultPath(long jobID, int rep) {
		return FileOperator.slaveRepPath(jobID, rep) + "/" + Parameters.resultFileName;
	}
	
	/**
	 * Get data file path of jobID in master
	 * @param jobID JobID
	 * @return Master data file path of jobID
	 */
	public static String masterDataPath(long jobID) {
		return master.Parameters.masterDataPath + "/" + jobID + "/" + Parameters.dataFileName;
	}
	
	/**
	 * Get replication directory path of rep of jobID
	 * @param jobID JobID
	 * @param rep Replication number
	 * @return Master path of replication number rep
	 */
	public static String masterRepPath(long jobID, int rep) {
		return master.Parameters.masterResultPath + "/" + jobID + "/" + rep;
	}
	
	/**
	 * Get result file path of rep of jobID
	 * @param jobID JobID
	 * @param rep Replication number
	 * @return Result file path of replication number rep
	 */
	public static String masterResultPath(long jobID, int rep) {
		return FileOperator.masterRepPath(jobID, rep) + "/" + Parameters.resultFileName;
	}
}
