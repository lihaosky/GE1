package common.command;


public class InitJobAck extends Command {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * JobID of client. < 0 if error in server
	 */
	public long jobID;
	public InitJobAck(int id, long jobID) {
		super(id);
		this.jobID = jobID;
	}
}
