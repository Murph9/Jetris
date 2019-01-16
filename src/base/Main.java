package base;

import com.jme3.app.SimpleApplication;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.MouseAppState;


public class Main extends SimpleApplication {

	public static Main CURRENT = new Main();
	
	public static void main(String[] args) {
		Main app = new Main();
		CURRENT = app;
//		app.setShowSettings(false);
		app.setDisplayStatView(false); //defaults to on, shows the triangle count and stuff
		app.start();
	}
	
	private MenuState menuState;
	private PlayState playState;
	
	private BackgroundSpaceState spaceState;
	
	@Override
	public void simpleInitApp() {
		//remove the default esc exit
		inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
		
		//engine disables
		flyCam.setEnabled(false);
		
		//initialize Lemur (GUI thing)
		GuiGlobals.initialize(this);
		//Load my style
		LemurGuiStyle.load(assetManager);
		//Init the lemur mouse listener
		getStateManager().attach(new MouseAppState(this));
		
		//spaceState init
		spaceState = new BackgroundSpaceState(100);
		getStateManager().attach(spaceState);
		
		//start game
		menuState = new MenuState(this);
		getStateManager().attach(menuState);
	}
	
	public void startPlay() {
		getStateManager().detach(menuState);
		menuState = null;
		
		playState = new PlayState(this);
		getStateManager().attach(playState);
	}
	
	public void gameLost(Record r) {
		getStateManager().detach(playState);
		playState = null;
		
		//save score
		RecordManager.saveRecord(r, "A", 0);
		
		menuState = new MenuState(this);
		getStateManager().attach(menuState);
	}
}
