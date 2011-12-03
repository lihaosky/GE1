package master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Class manage the nodes
 * @author lihao
 *
 */
public class NodeManager {
	private static ArrayList<Node> nodeList = new ArrayList<Node>();
	
	/**
	 * Find node list
	 */
	public static boolean findNodes() {
		int n = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(Parameters.nodeListFile));
			String line = br.readLine();
			while (line != null) {
				line = line.trim();
				if (!line.equals("")) {
					nodeList.add(new Node(line, Node.AVAILABLE));
					n++;
				}
				line = br.readLine();
			}
			System.out.println("Found " + n + " slave nodes!");
			return true;
		} catch (FileNotFoundException e) {
			System.err.println("Can't find file " + Parameters.nodeListFile);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println("Read file error!");
			e.printStackTrace();
			return false;
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get n nodes
	 * @param n number of nodes to get
	 */
	synchronized public static ArrayList<Node> getNodes(int n) {
		int addedNum = 0;
		ArrayList<Node> newList = new ArrayList<Node>();
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			if (node.getStatus() == Node.AVAILABLE) {
				//Try to connect to the node
				if (node.connect() == null) {
					System.out.println("Find slave error!");
					node.setStatus(Node.DEAD);
				} else {
					node.setStatus(Node.BUSY);
					newList.add(node);
					addedNum++;
					if (addedNum == n) {
						break;
					}
				}
			}
		}
		return newList;
	}
	
	/**
	 * May create a node: start an instance
	 */
	public static void createNode() {
		
	}
}
