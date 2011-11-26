package common;

public class DownloadCommand extends Command {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long fileLength;
	
	public DownloadCommand(int id, long fileLength) {
		super(id);
		this.fileLength = fileLength;
	}
	
}
