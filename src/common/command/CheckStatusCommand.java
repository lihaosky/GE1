package common.command;


public class CheckStatusCommand extends Command {

	private static final long serialVersionUID = 1L;
	public long jobID;
	
	public CheckStatusCommand(int id, long jobID) {
		super(id);
		this.jobID = jobID;
	}
	
}
