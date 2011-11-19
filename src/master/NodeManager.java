package master;

import java.util.ArrayList;


/**
 * Class manage the nodes
 * @author lihao
 *
 */
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
	synchronized public static ArrayList<Node> getNodes(int n) {
		ArrayList<Node> newList = new ArrayList<Node>();
		Node node = new Node("locahost", Node.AVAILABLE);
		if (!node.findHandler()) {
			System.out.println("Find handler error!");
			node.setStatus(Node.DEAD);
			return null;
		}
		node.setStatus(Node.BUSY);
		newList.add(node);
		return newList;
	}
	
	/**
	 * May create a node: start an instance
	 */
	public static void createNode() {
		
	}
}
