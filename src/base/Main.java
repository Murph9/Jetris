package base;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.MouseAppState;

import saving.Record;
import saving.RecordManager;


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
	private AiPlayState aiPlayState;

	private BackgroundState spaceState;
	
	@Override
	public void simpleInitApp() {
		//remove the default keys
		inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_HIDE_STATS);
		inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_CAMERA_POS); //TODO doesn't work
		inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_MEMORY);
		inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
		
		//initialize Lemur (GUI thing)
		GuiGlobals.initialize(this);
		//Load my style
		LemurGuiStyle.load(assetManager);
		//Init the lemur mouse listener
		getStateManager().attach(new MouseAppState(this));

		//some camera state stuff
		flyCam.setEnabled(false);
		getCamera().setLocation(new Vector3f());
		getCamera().lookAt(new Vector3f(1,0,0), Vector3f.UNIT_Y);
		getViewPort().setBackgroundColor(ColorRGBA.Black);
		
		//spaceState init
		spaceState = new BackgroundState(100);
		getStateManager().attach(spaceState);

		//start game
		menuState = new MenuState(this);
		getStateManager().attach(menuState);

		//load ai to give some background motion 
		aiPlayState = new AiPlayState(this);
		getStateManager().attach(aiPlayState);
	}
	
	public void startPlay() {
		getStateManager().detach(aiPlayState);
		aiPlayState = null;
		getStateManager().detach(menuState);
		menuState = null;
		
		playState = new PlayState(this);
		getStateManager().attach(playState);
	}
	
	public void gameLost(PlayState state, Record r) {
		if (state instanceof AiPlayState) {
			//don't save record, and reload
			getStateManager().detach(aiPlayState);
			aiPlayState = new AiPlayState(this);
			getStateManager().attach(aiPlayState);

		} else {

			getStateManager().detach(playState);
			playState = null;
			
			//save score
			RecordManager.saveRecord(r, "A");
			
			menuState = new MenuState(this);
			getStateManager().attach(menuState);

			aiPlayState = new AiPlayState(this);
			getStateManager().attach(aiPlayState);
		}
	}
}
