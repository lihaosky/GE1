package common.command;


public class CheckStatusAck extends Command {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1956445694054656635L;
	public int finishedRep;
	
	public CheckStatusAck(int id, int finishedRep) {
		super(id);
		this.finishedRep = finishedRep;
	}

}
