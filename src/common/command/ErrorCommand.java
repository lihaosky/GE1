package common.command;


public class ErrorCommand extends Command {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5403221405953481729L;
	public String message;
	
	public ErrorCommand(int id, String message) {
		super(id);
		this.message = message;
	}
	
}
