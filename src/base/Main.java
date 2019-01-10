package base;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.MouseAppState;

import logic.Tetris.GameType;

public class Main extends SimpleApplication {

	public static Main CURRENT = new Main();
	
	public static void main(String[] args) {
		Main app = CURRENT;
		//app.setShowSettings(false);
		app.setDisplayStatView(false); //defaults to on, shows the triangle count and stuff
		app.start();
	}
	
	private MenuState menuState;
	private PlayState playState;
	
	@Override
	public void simpleInitApp() {
		//remove the default esc exit
		inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
		
		//engine disables
		flyCam.setEnabled(false);
		getCamera().setLocation(new Vector3f());
		getCamera().lookAt(new Vector3f(1,0,0), Vector3f.UNIT_Y);
		inputManager.setCursorVisible(false);
		
		getViewPort().setBackgroundColor(ColorRGBA.White);
		
		
		//initialize Lemur (GUI thing)
		GuiGlobals.initialize(this);
		//Load my style
		LemurGuiStyle.load(assetManager);
		//Init the lemur mouse listener
		getStateManager().attach(new MouseAppState(this));
		
		
		//start game
		menuState = new MenuState(this);
		getStateManager().attach(menuState);
	}
	
	public void start(GameType type, int lineCount) {
		getStateManager().detach(menuState);
		menuState = null;
		
		playState = new PlayState(this, type, lineCount);
		getStateManager().attach(playState);
	}
	
	public void gameLost(GameType type, int bLineCount, Record r) {
		getStateManager().detach(playState);
		playState = null;
		
		//save score
		RecordManager.saveRecord(r, type, bLineCount);
		
		menuState = new MenuState(this);
		getStateManager().attach(menuState);
	}
}
