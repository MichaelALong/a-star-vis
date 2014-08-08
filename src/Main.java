import java.io.File;

import org.lwjgl.LWJGLUtil;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

/**
 * Main class for A* visualization.
 * @author Michael A. Long
 */
public class Main {

	public static void main(String[] args) {
		System.setProperty("org.lwjgl.librarypath", new File(new File(System.getProperty("user.dir"), "native")
				, LWJGLUtil.getPlatformName()).getAbsolutePath());
		
		try {
			AppGameContainer app = new AppGameContainer(new Demo());
			app.setDisplayMode(745, 600, false);
			app.setTargetFrameRate(60);
			app.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

}
