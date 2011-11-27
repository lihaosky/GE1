package common.command;

import java.util.ArrayList;


public class AddRepCommand extends Command {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5829009675713104276L;
	public ArrayList<Integer> repList;
	public AddRepCommand(int id, ArrayList<Integer> repList) {
		super(id);
		this.repList = repList;
	}

}
