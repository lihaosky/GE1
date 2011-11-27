package common.command;

import java.io.*;

public class Command implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2054962418881053966L;
	public int commandID;
	
	public Command(int id) {
		commandID = id;
	}
	
	public static int InitJobCommand = 0;
	public static int InitJobAck = 1;
	public static int CheckStatusCommand = 2;
	public static int CheckStatusAck = 3;
	public static int DownloadCommand = 4;
	public static int DownloadAck = 5;
	public static int InitAssignmentCommand = 6;
	public static int DownloadRepCommand = 7;
	public static int AddRepCommand = 8;
	public static int ErrorMessage = 9;
	public static int ErrorAck = 10;
}
