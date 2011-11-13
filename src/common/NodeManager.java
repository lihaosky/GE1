package common;

import java.util.ArrayList;

public class NodeManager {
	private static ArrayList<Node> nodeList;
	
	/**
	 * Find node list
	 */
	public static void findNodes() {
		
	}
	
	/**
	 * Get n nodes
	 * @param n number of nodes to get
	 */
	public static ArrayList<Node> getNodes(int n) {
		ArrayList<Node> newList = new ArrayList<Node>();
		Node node = new Node("locahost", Node.AVAILABLE);
		node.findHandler();
		newList.add(node);
		return newList;
	}
	
	/**
	 * May create a node: start an instance
	 */
	public static void createNode() {
		
	}
}
