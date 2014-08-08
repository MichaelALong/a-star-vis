import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.MouseOverArea;

/**
 * GUI container for A* project.
 * Built with Slick2D/LWJGL. http://slick.ninjacave.com/
 * @author Michael A. Long
 *
 */
public class Demo extends BasicGame implements ComponentListener, MouseListener {
	World world;//our graph, a 2D grid of vertices.
	MouseOverArea[] areas = new MouseOverArea[7];//clickable "buttons"
	Image[] buttonImages = new Image[14];//images for the buttons
	int activeButton;//tracks which action to perform on the graph when a click occurs
	Input input;
	private String message = "";//words drawn onto bottom of window
	final int nodewidth = 20;//drawing width of vertices (Nodes) in our graph
	

	public Demo() throws SlickException {
		super("A Star Demonstration by Michael A. Long");
		world = new World(27,27);
		activeButton = 0;
	}
	/**
	 * Draws graph grid, buttons, etc into the window. Called continually.
	 */
	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		g.setBackground(Color.white);
		g.clear();
		g.setColor(Color.black);
		for (int y=0; y<world.height; y++) {
			for (int x=0; x<world.width; x++) {
				g.drawRect(10+(nodewidth/2)+x*nodewidth, 10+(nodewidth/2)+y*nodewidth,
					nodewidth, nodewidth);
			}
		}
		
		for (int i=0;i<areas.length;i++) {
			areas[i].render(gc, g);
		}
		drawSolution(gc, g);
		if (world.hasSolution) g.setColor(Color.blue);
		else g.setColor(Color.red);
		g.drawString(message, 240, 570);
		
		//draw graph legend
		g.setColor(Color.black);
		g.drawString("Graph vertex", 570, 420);
		g.drawRect(705, 420, nodewidth, nodewidth);
		
		g.setColor(Color.black);
		g.drawString("Checked vertex", 570, 450);
		g.drawRect(705, 450, nodewidth, nodewidth);
		Color LightSteelBlue = new Color(176, 196, 222);
		g.setColor(LightSteelBlue);
		g.fillRect(705+3, 450+3, nodewidth-5, nodewidth-5);
		
		g.setColor(Color.black);
		g.drawString("Path vertex", 570, 480);
		g.drawRect(705, 480, nodewidth, nodewidth);
		Color FireBrick = new Color(178, 34, 34);
		g.setColor(FireBrick);
		g.fillRect(705+3, 480+3, nodewidth-5, nodewidth-5);
		
		g.setColor(Color.black);
		g.drawString("Start vertex", 570, 510);
		g.drawRect(705, 510, nodewidth, nodewidth);
		Color SeaGreen = new Color(46, 139, 87);
		g.setColor(SeaGreen);
		g.fillRect(705+3, 510+3, nodewidth-5, nodewidth-5);
		
		g.setColor(Color.black);
		g.drawString("End vertex", 570, 540);
		g.drawRect(705,  540, nodewidth, nodewidth);
		Color MediumBlue = new Color(0, 0, 205);
		g.setColor(MediumBlue);
		g.fillRect(705+3, 540+3, nodewidth-5, nodewidth-5);
	}

	/**
	 * Initialize the GUI. Loads images from folder.
	 * Occurs before window is drawn for the first time.
	 */
	@Override
	public void init(GameContainer gc) throws SlickException {
		gc.setShowFPS(false);
		
		world.startX = 0;
		world.startY = 0;
		world.endX = 26;
		world.endY = 26;
		world.initAStar();
		world.randomizeWalls();
		world.reinitAStar();
		world.runAStar();
		//world.writeSolution();
		buttonImages[0] = new Image("assets\\set_start1.png");
		buttonImages[1] = new Image("assets\\set_end1.png");
		buttonImages[2] = new Image("assets\\make_block1.png");
		buttonImages[3] = new Image("assets\\clear_block1.png");
		buttonImages[4] = new Image("assets\\randomize1.png");
		buttonImages[5] = new Image("assets\\randomize2.png");
		buttonImages[6] = new Image("assets\\randomize3.png");
		buttonImages[7] = new Image("assets\\set_start2.png");
		buttonImages[8] = new Image("assets\\set_end2.png");
		buttonImages[9] = new Image("assets\\make_block2.png");
		buttonImages[10] = new Image("assets\\clear_block2.png");
		buttonImages[11] = new Image("assets\\randomize1.png");
		buttonImages[12] = new Image("assets\\randomize2.png");
		buttonImages[13] = new Image("assets\\randomize3.png");
		
		for (int i=0;i<areas.length;i++) {
			int imageIndex = i;
			if (activeButton == i) imageIndex = 7+i;
			areas[i] = new MouseOverArea(gc, buttonImages[imageIndex],
					580, 30 + (i*50), buttonImages[imageIndex].getWidth(),
					buttonImages[imageIndex].getHeight(), this);
			areas[i].setNormalColor(new Color(1,1,1,0.8f));
			areas[i].setMouseOverColor(new Color(1,1,1,0.9f));
		}
	}

	@Override
	public void update(GameContainer gc, int arg1) throws SlickException {
	}
	
	/**
	 * Draws the solution path, which was found using A*, onto the screen.
	 * @param gc
	 * @param g
	 */
	public void drawSolution(GameContainer gc, Graphics g) {
		int adjwidth = nodewidth-5;
		Node cur = world.grid[world.endY][world.endX];
		for (int y=0; y<world.height; y++) {
			for (int x=0; x<world.width; x++) {
				//draw vertices which A* visited
				if (world.grid[y][x].status == NodeStatus.CLOSED ||
						world.grid[y][x].status == NodeStatus.OPEN) {
					Color LightSteelBlue = new Color(176, 196, 222);
					g.setColor(LightSteelBlue);
					g.fillRect(10+(nodewidth/2)+x*nodewidth+3,
							10+(nodewidth/2)+y*nodewidth+3, adjwidth, adjwidth);
				}
				if (world.grid[y][x].blocked) {
					g.setColor(Color.darkGray);
					//g.fillRect(10+(nodewidth/2)+x*nodewidth+3
					//		,10+(nodewidth/2)+y*nodewidth+3, adjwidth, adjwidth);
					g.fillRect(10+(nodewidth/2)+x*nodewidth
							,10+(nodewidth/2)+y*nodewidth, nodewidth, nodewidth);
				}
			}
		}
		//draw vertices in solution path
		Color FireBrick = new Color(178, 34, 34);
		g.setColor(FireBrick);
		while (cur != null) {
			g.fillRect(10+(nodewidth/2)+cur.xloc*nodewidth+3,
					10+(nodewidth/2)+cur.yloc*nodewidth+3, adjwidth, adjwidth);
			cur = cur.parent;
		}
		//draw connecting line over solution path
		cur = world.grid[world.endY][world.endX];
		while (cur.parent != null) {
			g.setColor(FireBrick);
			g.setLineWidth(6.0f);
			if (cur.parent.xloc != cur.xloc && cur.parent.yloc != cur.yloc)
				g.setLineWidth(9.0f);//thicker width for diagonal lines
			g.drawLine(10+(nodewidth/2)+cur.xloc*nodewidth + (nodewidth/2),
					10+(nodewidth/2)+cur.yloc*nodewidth + (nodewidth/2), 
					10+(nodewidth/2)+cur.parent.xloc*nodewidth + (nodewidth/2), 
					10+(nodewidth/2)+cur.parent.yloc*nodewidth + (nodewidth/2));
			g.setLineWidth(1.0f);
			cur = cur.parent;
		}
		//draw start and end vertices
		Color SeaGreen = new Color(46, 139, 87);
		g.setColor(SeaGreen);
		g.fillRect(10+(nodewidth/2)+world.startX*nodewidth+3,
				10+(nodewidth/2)+world.startY*nodewidth+3, adjwidth, adjwidth);
		Color MediumBlue = new Color(0, 0, 205);
		//Color DodgerBlue = new Color(30, 144, 255);
		g.setColor(MediumBlue);
		g.fillRect(10+(nodewidth/2)+world.endX*nodewidth+3,
				10+(nodewidth/2)+world.endY*nodewidth+3, adjwidth, adjwidth);
		if (world.hasSolution) {
			message = "Solution found";
		} else {
			message = "No solution";
		}
	}
	/**
	 * Changes button images, detects which button received a click,
	 * and responds to randomization button clicks. Occurs before
	 * mouseClicked(). Function is triggered by mouse clicks.
	 */
	@Override
	public void componentActivated(AbstractComponent source) {
		for (int i=0;i<areas.length;i++) {
			if (source == areas[i]) {
				//System.out.println("option "+i+" pressed");
				areas[activeButton].setNormalImage(buttonImages[activeButton]);
				areas[activeButton].setMouseOverImage(buttonImages[activeButton]);
				if (i != 4 && i!= 5 && i != 6) activeButton = i;
				areas[activeButton].setNormalImage(buttonImages[7+activeButton]);
				areas[activeButton].setMouseOverImage(buttonImages[7+activeButton]);
				
				if (i == 4) {
					world.randomizeWalls();
					world.reinitAStar();
					world.runAStar();
				} else if (i == 5) {
					world.randomizeWalls2();
					world.reinitAStar();
					world.runAStar();
				} else if (i == 6) {
					world.randomizeWalls3();
					world.reinitAStar();
					world.runAStar();
				}
				//if (areas[activeButton].isAcceptingInput())
				//	System.out.println("mouse  areas["+i+"]");
			}
		}
	}
	
	public void mouseClicked(int button, int x, int y, int clickCount) {
		int gridminx = 10+(nodewidth/2);
		int gridminy = 10+(nodewidth/2);
		int gridmaxx = 10+(nodewidth/2)+world.width*nodewidth;
		int gridmaxy = 10+(nodewidth/2)+world.height*nodewidth;
		
		if (x>=gridminx && x<gridmaxx && y>=gridminy && y<gridmaxy) {
			int col = (x - gridminx) / nodewidth;
			int row = (y - gridminy) / nodewidth;
			//System.out.println("inside grid ("+col+","+row+")");
			gridClicked(col, row);
		}
	}
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		int gridminx = 10+(nodewidth/2);
		int gridminy = 10+(nodewidth/2);
		int gridmaxx = 10+(nodewidth/2)+world.width*nodewidth;
		int gridmaxy = 10+(nodewidth/2)+world.height*nodewidth;
		
		if (newx>=gridminx && newx<gridmaxx && newy>=gridminy && newy<gridmaxy) {
			int col = (newx - gridminx) / nodewidth;
			int row = (newy - gridminy) / nodewidth;
			//System.out.println("dragged at grid ("+col+","+row+")");
			gridClicked(col, row);
		}
	}
	/**
	 * Performs action when user clicks on the graph, based on
	 * which button is currently active (because it was clicked).
	 * @param col
	 * @param row
	 */
	public void gridClicked(int col, int row) {
		//activeButton: 0: set start, 1:set end, 2:make block, 3:clear block 
		switch (activeButton) {
		case 0://set start
			if (!world.grid[row][col].blocked) {
				world.startX = col;
				world.startY = row;				
			}
			break;
		case 1://set end
			if (!world.grid[row][col].blocked) {
				world.endX = col;
				world.endY = row;
			}
			break;
		case 2://make block
			if (!(world.endX==col && world.endY==row) && !(world.startX==col && world.startY==row)) {
				world.grid[row][col].blocked = true;
			}
			break;
		case 3://clear block
			world.grid[row][col].blocked = false;
			break;
		}
		world.reinitAStar();
		world.runAStar();
	}

}
