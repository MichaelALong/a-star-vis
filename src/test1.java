
public class test1 {

	public static void main(String[] args) {
		//Node myNode = new Node();
		World w;
		w = new World(10,10);
		w.startX = 0;
		w.startY = 0;
		w.endX = 9;
		w.endY = 9;
		w.initAStar();
		w.runAStar();
		w.writeSolution();
		/*int height = 10; int width = 10;
		xNode[][] grid = new xNode[height][width];
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				grid[y][x] = new xNode();
				grid[y][x].blocked = false;
				grid[y][x].parent = null;
				grid[y][x].status = NodeStatus.UNSEEN;
				grid[y][x].xloc = x;
				grid[y][x].yloc = y;
			}
		}*/

	}

}
