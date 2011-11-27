package common.command;


public class DownloadRepCommand extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4348926547757488811L;
	public int repNum;
	public long fileLength;
	
	public DownloadRepCommand(int id, int repNum, long fileLength) {
		super(id);
		this.repNum = repNum;
		this.fileLength = fileLength;
	}

}
