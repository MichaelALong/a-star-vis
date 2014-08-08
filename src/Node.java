
/**
 * Represents a single vertex in a graph
 * @author Michael A. Long
 *
 */
public class Node {
	public int xloc;
	public int yloc;
	public NodeStatus status;//whether A* has checked this vertex yet, or added it to the queue
	public boolean blocked;//node is inaccessible and can't be used in a path
	public double F;// F = G + H
	public double G;// distance from start point to this vertex
	public double H;// estimated distance to goal
	public Node parent;//the previous vertex we visited to reach this node
	
	public Node() {
		xloc = -1;
		yloc = -1;
		status = NodeStatus.UNSEEN;
		blocked = false;
		this.F = 0.0;
		this.G = 0.0;
		this.H = 0.0;
		parent = null;
	}
	public void calcF() {
		this.F = this.G + this.H;
	}
	
}
