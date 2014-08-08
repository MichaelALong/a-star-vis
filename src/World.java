import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Holds the Nodes which represent the vertices for this simulation.
 * Contains maze generating routines, and A* path finding
 * @author Michael A. Long
 *
 */
public class World {
	public int width;
	public int height;
	public int startX;
	public int startY;
	public int endX;
	public int endY;
	public boolean hasSolution;
	public Node[][] grid;
	public PriorityQueue<Node> openqueue;
	
	public World(int _width, int _height) {
		width = _width;
		height = _height;
		startX = startY = endX = endY = -1;
		grid = new Node[height][width];
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				grid[y][x] = new Node();
				grid[y][x].blocked = false;
				grid[y][x].parent = null;
				grid[y][x].status = NodeStatus.UNSEEN;
				grid[y][x].xloc = x;
				grid[y][x].yloc = y;
			}
		}
		openqueue = new PriorityQueue<Node>(30,new CostComparator());
	}
	/**
	 * Distance estimation heuristic
	 * Manhattan distance (Euclidean is commented).
	 */
	public void setHeuristicAll() {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				int xdiff = Math.abs(grid[y][x].xloc - grid[endY][endX].xloc);
				int ydiff = Math.abs(grid[y][x].yloc - grid[endY][endX].yloc);
				//int diffSquared = (xdiff * xdiff) + (ydiff * ydiff);
				//grid[y][x].H = Math.sqrt((double)diffSquared);
				grid[y][x].H = xdiff + ydiff;
			}
		}
	}
	public void runAStar() {
		grid[endY][endX].blocked = false;
		grid[startY][startX].blocked = false;
		boolean foundSolution = false;
		while (!openqueue.isEmpty()) {
			Node cur = openqueue.remove();
			if (cur.xloc == endX && cur.yloc == endY) {
				foundSolution = true;
				break;
			}
			cur.status = NodeStatus.CLOSED;
			//check all accessible adjacent xNodes
			ArrayList<Node> neighbors = new ArrayList<>();
			if (cur.yloc > 0) {//check above
				Node adj = grid[cur.yloc-1][cur.xloc];//check up
				if (!adj.blocked)
					neighbors.add(adj);
				//uncomment for diagonal pathfinding
			/*	if (cur.xloc > 0) {
					adj = grid[cur.yloc-1][cur.xloc-1];//up-left
					if (!adj.blocked)
						neighbors.add(adj);
				}
				if (cur.xloc < width-1) {
					adj = grid[cur.yloc-1][cur.xloc+1];//up-right
					if (!adj.blocked)
						neighbors.add(adj);
				}     */
			}
			if (cur.yloc < height-1) {//check below
				Node adj = grid[cur.yloc+1][cur.xloc];//down
				if (!adj.blocked)
					neighbors.add(adj);
				//uncomment for diagonal pathfinding
			 /*	if (cur.xloc > 0) {
					adj = grid[cur.yloc+1][cur.xloc-1];//down-left
					if (!adj.blocked)
						neighbors.add(adj);
				}
				if (cur.xloc < width-1) {
					adj = grid[cur.yloc+1][cur.xloc+1];//down-right
					if (!adj.blocked)
						neighbors.add(adj);
				}  	*/
			}
			if (cur.xloc > 0) {
				Node adj = grid[cur.yloc][cur.xloc-1];//left
				if (!adj.blocked)
					neighbors.add(adj);
			}
			if (cur.xloc < width-1) {
				Node adj = grid[cur.yloc][cur.xloc+1];//right
				if (!adj.blocked)
					neighbors.add(adj);
			}
			for (Node n : neighbors) {
				double dist = cur.G + getDistance(cur, n);
				if (n.status == NodeStatus.UNSEEN) {
					n.status = NodeStatus.OPEN;					
					n.G = dist;
					n.calcF();
					n.parent = cur;
					openqueue.add(n);
				} else { //node previously visited (closed), or queued (open)
					//update this node if the new cost is lower
					double totalcost = n.H + dist;
					if (totalcost < n.F) {
						if (n.status == NodeStatus.CLOSED) {
							n.status = NodeStatus.OPEN;
							n.G = dist;
							n.calcF();
							n.parent = cur;
							openqueue.add(n);
						} else {//node is currently queued
							openqueue.remove(n);
							n.status = NodeStatus.OPEN;
							n.G = dist;
							n.calcF();
							n.parent = cur;
							openqueue.add(n);							
						}
					}
					
				}
			}
		}
		if (!foundSolution) {
			hasSolution = false;
			//log("failed to find solution");
		} else {
			hasSolution = true;
			//log("found a solution!");
		}
	}
	public void writeSolution() {
		Node cur = grid[endY][endX];
		while (cur != null) {
			String str = "("+cur.xloc+","+cur.yloc+")";
			str += "F:"+cur.F+" G:"+cur.G+" H:"+cur.H;
			log(str);
			cur = cur.parent;
		}
	}
	public void initAStar() {
		if (startX<0||startX>=width||endX<0||endX>=width
				||startY<0||startY>=height||endY<0||endY>=height) {
			log("ERROR: invalid initialization values!");
		}
		setHeuristicAll();
		openqueue.clear();
		grid[startY][startX].G = 0.0;
		grid[startY][startX].calcF();
		grid[startY][startX].parent = null;
		openqueue.add(grid[startY][startX]);
	}
	/**
	 * Re-initializes the graph in preparation
	 * for another A* run.
	 */
	public void reinitAStar() {
		if (startX<0||startX>=width||endX<0||endX>=width
				||startY<0||startY>=height||endY<0||endY>=height) {
			log("ERROR: invalid initialization values!");
		}

		openqueue.clear();
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				grid[y][x].F = 0.0;
				grid[y][x].G = 0.0;
				grid[y][x].H = 0.0;
				grid[y][x].parent = null;
				grid[y][x].status = NodeStatus.UNSEEN;
			}
		}
		
		grid[startY][startX].calcF();		
		setHeuristicAll();
		openqueue.add(grid[startY][startX]);
	}
	public double getDistance(Node n1, Node n2) {
		int xdiff = Math.abs(n1.xloc - n2.xloc);
		int ydiff = Math.abs(n1.yloc - n2.yloc);
		double diffSq = (xdiff * xdiff) + (ydiff * ydiff);
		return Math.sqrt(diffSq);
	}
	public static void log(String str) {
		System.out.println(str);
	}
	/**
	 * Uses a variation of Prim's algorithm for maze generation
	 * Result has wider passages than Prim's.
	 * Passages are generally 1-3 vertices wide.
	 */
	public void randomizeWalls3() {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				grid[y][x].blocked = true;
				grid[y][x].status = NodeStatus.UNSEEN;
			}
		}
		Random rand = new Random();
		ArrayList<Node> nearWalls = new ArrayList<>();
		grid[0][0].blocked = false;
		nearWalls.add(grid[0][1]);
		nearWalls.add(grid[1][0]);
		while (!nearWalls.isEmpty()) {
			Node cur = nearWalls.remove(rand.nextInt(nearWalls.size()));
			boolean expanded = false;
			ArrayList<Integer> order = new ArrayList<>();
			for (int i=1; i<=4; i++) order.add(new Integer(i));
			while (!order.isEmpty() && !expanded) {
				int num = order.remove(rand.nextInt(order.size()));
				switch (num) {
				case 1://right
					if (cur.xloc < width-1) {
						if (grid[cur.yloc][cur.xloc+1].blocked && grid[cur.yloc][cur.xloc+1].status == NodeStatus.UNSEEN) {
							cur.blocked = false;
							expanded = true;
						}
					}
					break;
				case 2://down
					if (cur.yloc < height-1) {
						if (grid[cur.yloc+1][cur.xloc].blocked && grid[cur.yloc+1][cur.xloc].status == NodeStatus.UNSEEN) {
							cur.blocked = false;
							expanded = true;
						}
					}
					break;
				case 3://left
					if (cur.xloc > 0) {
						if (grid[cur.yloc][cur.xloc-1].blocked && grid[cur.yloc][cur.xloc-1].status == NodeStatus.UNSEEN) {
							cur.blocked = false;
							expanded = true;
						}
					}
					break;
				case 4://up
					if (cur.yloc > 0) {
						if (grid[cur.yloc-1][cur.xloc].blocked && grid[cur.yloc-1][cur.xloc].status == NodeStatus.UNSEEN) {
							cur.blocked = false;
							expanded = true;
						}
					}
					break;
				}
			
			}//end while
			if (!expanded) {
				cur.status = NodeStatus.CLOSED;
			} else {
				if (cur.xloc > 0) {
					Node nei = grid[cur.yloc][cur.xloc-1];
					if (nei.blocked && nei.status == NodeStatus.UNSEEN) {
						nei.status = NodeStatus.OPEN;
						nearWalls.add(nei);
					}
				}
				if (cur.xloc < width-1) {
					Node nei = grid[cur.yloc][cur.xloc+1];
					if (nei.blocked && nei.status == NodeStatus.UNSEEN) {
						nei.status = NodeStatus.OPEN;
						nearWalls.add(nei);
					}
				}
				if (cur.yloc > 0) {
					Node nei = grid[cur.yloc-1][cur.xloc];
					if (nei.blocked && nei.status == NodeStatus.UNSEEN) {
						nei.status = NodeStatus.OPEN;
						nearWalls.add(nei);
					}
				}
				if (cur.yloc < height-1) {
					Node nei = grid[cur.yloc+1][cur.xloc];
					if (nei.blocked && nei.status == NodeStatus.UNSEEN) {
						nei.status = NodeStatus.OPEN;
						nearWalls.add(nei);
					}
				}
				cur.status = NodeStatus.CLOSED;
				//System.out.println("closed ("+cur.xloc+","+cur.yloc+")");
			}
		}
	}
	/**
	 * Uses a variation of Prim's algorithm for maze generation
	 * Result is similar to Prim's, but with less regularity in braching
	 */
	public void randomizeWalls2() {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				grid[y][x].blocked = true;
				grid[y][x].status = NodeStatus.UNSEEN;
			}
		}
		int totalopen = 0;
		Random rand = new Random();
		ArrayList<Node> nearWalls = new ArrayList<>();
		grid[0][0].blocked = false;
		grid[0][0].status = NodeStatus.CLOSED;
		nearWalls.add(grid[0][1]);
		nearWalls.add(grid[1][0]);
		while (!nearWalls.isEmpty()) {
			//if (totalopen > 5) return;
			Node cur = nearWalls.remove(rand.nextInt(nearWalls.size()));
			int adjopen = 0;
			boolean expanded = false;
			boolean skip = false;			
			if (cur.xloc > 0) {
				if (!grid[cur.yloc][cur.xloc-1].blocked) adjopen++;
			}
			if (cur.xloc < width-1) {
				if (!grid[cur.yloc][cur.xloc+1].blocked) adjopen++;
			}
			if (cur.yloc > 0) {
				if (!grid[cur.yloc-1][cur.xloc].blocked) adjopen++;
			}
			if (cur.yloc < height-1) {
				if (!grid[cur.yloc+1][cur.xloc].blocked) adjopen++;
			}
			if (adjopen >= 2) skip = true;
			
			ArrayList<Integer> order = new ArrayList<>();
			for (int i=1; i<=4; i++) order.add(new Integer(i));
			while (!order.isEmpty() && !expanded && !skip) {
				int num = order.remove(rand.nextInt(order.size()));
				switch (num) {
				case 1://right
					if (cur.xloc < width-1) {
						if (grid[cur.yloc][cur.xloc+1].blocked && grid[cur.yloc][cur.xloc+1].status == NodeStatus.UNSEEN) {
							cur.blocked = false; totalopen++;
							expanded = true;
						}
					}
					break;
				case 2://down
					if (cur.yloc < height-1) {
						if (grid[cur.yloc+1][cur.xloc].blocked && grid[cur.yloc+1][cur.xloc].status == NodeStatus.UNSEEN) {
							cur.blocked = false; totalopen++;
							expanded = true;
						}
					}
					break;
				case 3://left
					if (cur.xloc > 0) {
						if (grid[cur.yloc][cur.xloc-1].blocked && grid[cur.yloc][cur.xloc-1].status == NodeStatus.UNSEEN) {
							cur.blocked = false; totalopen++;
							expanded = true;
						}
					}
					break;
				case 4://up
					if (cur.yloc > 0) {
						if (grid[cur.yloc-1][cur.xloc].blocked && grid[cur.yloc-1][cur.xloc].status == NodeStatus.UNSEEN) {
							cur.blocked = false; totalopen++;
							expanded = true;
						}
					}
					break;
				}
			
			}//end while
			if (!expanded) {
				cur.status = NodeStatus.CLOSED;
			} else {
				if (cur.xloc > 0) {
					Node nei = grid[cur.yloc][cur.xloc-1];
					if (nei.blocked && nei.status == NodeStatus.UNSEEN) {
						nei.status = NodeStatus.OPEN;
						nearWalls.add(nei);
					}
				}
				if (cur.xloc < width-1) {
					Node nei = grid[cur.yloc][cur.xloc+1];
					if (nei.blocked && nei.status == NodeStatus.UNSEEN) {
						nei.status = NodeStatus.OPEN;
						nearWalls.add(nei);
					}
				}
				if (cur.yloc > 0) {
					Node nei = grid[cur.yloc-1][cur.xloc];
					if (nei.blocked && nei.status == NodeStatus.UNSEEN) {
						nei.status = NodeStatus.OPEN;
						nearWalls.add(nei);
					}
				}
				if (cur.yloc < height-1) {
					Node nei = grid[cur.yloc+1][cur.xloc];
					if (nei.blocked && nei.status == NodeStatus.UNSEEN) {
						nei.status = NodeStatus.OPEN;
						nearWalls.add(nei);
					}
				}
				cur.status = NodeStatus.CLOSED;
				//System.out.println("closed ("+cur.xloc+","+cur.yloc+")");
			}
		}
	}
	/**
	 * Uses Prim's Algorithm for maze generation
	 */
	public void randomizeWalls() {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				grid[y][x].blocked = true;
				grid[y][x].status = NodeStatus.UNSEEN;
			}
		}
		int totalopen = 0;
		Random rand = new Random();
		ArrayList<Pair<Node,Integer>> nearWalls = new ArrayList<>();
		grid[0][0].blocked = false; totalopen++;
		grid[0][0].status = NodeStatus.CLOSED;
		nearWalls.add(new Pair<>(grid[0][1], new Integer(1)));
		nearWalls.add(new Pair<>(grid[1][0], new Integer(2)));
		while (!nearWalls.isEmpty()) {
			//if (totalopen > 20) return;
			Pair<Node,Integer> cur = nearWalls.remove(rand.nextInt(nearWalls.size()));
			Node n = cur.getLeft();
			Integer dir = cur.getRight();
			if (!n.blocked) {
				//System.out.println("not blocked");
				continue;
			}
			//System.out.println("node ("+n.xloc+","+n.yloc+"), dir:"+dir);

			boolean expanded = false;
			boolean skip = false;

			if (dir.intValue() == 1) {
				if (n.xloc+1<width && grid[n.yloc][n.xloc+1].blocked) {
					n.blocked = false; n.status = NodeStatus.CLOSED; totalopen++;
					grid[n.yloc][n.xloc+1].blocked = false; totalopen++;
				}
			} else if (dir.intValue() == 2) {
				if (n.yloc+1<height && grid[n.yloc+1][n.xloc].blocked) {
					n.blocked = false; n.status = NodeStatus.CLOSED; totalopen++;
					grid[n.yloc+1][n.xloc].blocked = false; totalopen++;
				}
			} else if (dir.intValue() == 3) {
				if (n.xloc>0 && grid[n.yloc][n.xloc-1].blocked) {
					n.blocked = false; n.status = NodeStatus.CLOSED; totalopen++;
					grid[n.yloc][n.xloc-1].blocked = false; totalopen++;
				}
			} else {
				if (n.yloc>0 && grid[n.yloc-1][n.xloc].blocked) {
					n.blocked = false; n.status = NodeStatus.CLOSED; totalopen++;
					grid[n.yloc-1][n.xloc].blocked = false; totalopen++;
				}
			}
			
			if (!n.blocked) {
				int xoff = n.xloc;
				int yoff = n.yloc;
				if (dir.intValue() == 1) {
					xoff +=1;
				} else if (dir.intValue() == 2) {
					yoff +=1;
				} else if (dir.intValue() == 3) {
					xoff -= 1;
				} else {
					yoff -= 1;
				}
				//System.out.println("n: ("+n.xloc+","+n.yloc+")");
				//System.out.println("n2: ("+xoff+","+yoff+")");
				Node n2 = grid[yoff][xoff];
				
				if (n2.xloc > 1) {
					if (grid[n2.yloc][n2.xloc-1].blocked) {
						Pair<Node,Integer> p = new Pair<>(grid[n2.yloc][n2.xloc-1], new Integer(3));
						nearWalls.add(p);
					}
				}
				if (n2.xloc < width-1) {
					if (grid[n2.yloc][n2.xloc+1].blocked) {
						Pair<Node,Integer> p = new Pair<>(grid[n2.yloc][n2.xloc+1], new Integer(1));
						nearWalls.add(p);
					}
				}
				if (n2.yloc > 1) {
					if (grid[n2.yloc-1][n2.xloc].blocked) {
						Pair<Node,Integer> p = new Pair<>(grid[n2.yloc-1][n2.xloc], new Integer(4));
						nearWalls.add(p);
					}
				}
				if (n2.yloc < height-1) {
					if (grid[n2.yloc+1][n2.xloc].blocked) {
						Pair<Node,Integer> p = new Pair<>(grid[n2.yloc+1][n2.xloc], new Integer(2));
						nearWalls.add(p);
					}
				}
			}

		}//end while (!isEmpty())
	}
}

class Pair<L,R> {

	  private final L left;
	  private final R right;

	  public Pair(L left, R right) {
	    this.left = left;
	    this.right = right;
	  }

	  public L getLeft() { return left; }
	  public R getRight() { return right; }

	  @Override
	  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

	  @Override
	  public boolean equals(Object o) {
	    if (o == null) return false;
	    if (!(o instanceof Pair)) return false;
	    Pair pairo = (Pair) o;
	    return this.left.equals(pairo.getLeft()) &&
	           this.right.equals(pairo.getRight());
	  }

	}