package common;

public class InitJobCommand extends Command {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Replication number
	 */
	public int repNum;
	/**
	 * Time
	 */
	public int time;
	/**
	 * File length
	 */
	public long fileLength;
	
	public InitJobCommand(int id, int repNum, int time, long fileLength) {
		super(id);
		this.repNum = repNum;
		this.time = time;
		this.fileLength = fileLength;
	}
}
