package common;

public class DownloadAck extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1907801916893630229L;
	public int status;
	public DownloadAck(int id, int status) {
		super(id);
		this.status = status;
	}
}
