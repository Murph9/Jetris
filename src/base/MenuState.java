package base;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BorderLayout.Position;

import saving.ISettings;
import saving.Record;
import saving.RecordManager;
import saving.SettingsManager;
import saving.SettingsManager.Key;

public class MenuState extends BaseAppState {

	private HashMap<SettingsManager.Key, Checkbox> checkboxes;
	private Node rootNode;
	private Main main;
	
	private Container mainWindow;
	
	public MenuState(Main m) {
		main = m;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize(Application app) {
		//init gui
		//info window first so the event listeners can delete it
		this.rootNode = new Node("menu node");
		SimpleApplication sa = (SimpleApplication)app;
		sa.getGuiNode().attachChild(rootNode);
		
		Vector3f pos = new Vector3f(0, sa.getCamera().getHeight(), 0);
		
		mainWindow = new Container(new BorderLayout());
		mainWindow.setLocalTranslation(pos);
		rootNode.attachChild(mainWindow);
		
		Container buttonWindow = new Container();
		mainWindow.addChild(buttonWindow, Position.North);

		Button start = buttonWindow.addChild(new Button("Start"));
		start.addClickCommands(new Command<Button>() {
            @Override
            public void execute( Button source ) {
            	triggerSomething();
            }
        });
		
		TabbedPanel myWindow = new TabbedPanel();
		myWindow.addTab("Leaderboard", generateLeaderboardTab());
		myWindow.addTab("Info", generateInfoTab());
		myWindow.addTab("Settings", generateSettingsTab());
		
		mainWindow.addChild(myWindow, Position.Center);
		
		float width = CellHelper.fieldWidth(sa.getCamera().getHeight(), PlayState.X_SIZE/2, (PlayState.Y_SIZE-PlayState.Y_HIDDEN)/2)/2f;
		mainWindow.setPreferredSize(new Vector3f(sa.getCamera().getWidth()/2 - width, sa.getCamera().getHeight(), 0));
	}

	private Panel generateLeaderboardTab() {
		Container myWindow = new Container();
		myWindow.addChild(new Label("Leaderboard:"), 0, 0);
		
		myWindow.addChild(new Label("Score"), 1, 0);
		myWindow.addChild(new Label("Lines"), 1, 1);
	
		List<Record> records = RecordManager.getRecords("A");
		for (int i = 0; i < Math.min(records.size(), 10); i++) {
			Record r = records.get(i);
			myWindow.addChild(new Label(r.getScore() + (r.isNew() ? "*" : "")), 2 + i, 0);
			myWindow.addChild(new Label(r.getLineCount() + ""), 2 + i, 1);
		}
		return myWindow;
	}
	private Panel generateInfoTab() {
		Container c = new Container();
		c.addChild(new Label("ASD or arrows left,right for moving"));
		c.addChild(new Label("X and up for rotation with Ctrl and Z the other direction"));
		c.addChild(new Label("Esc and f1 for pausing"));
		c.addChild(new Label("Space for hard drop"));
		c.addChild(new Label("Shift and C for hold"));
		c.addChild(new Label(""));
		c.addChild(new Label("The numpad also works."));
		return c;
	}
	@SuppressWarnings("unchecked")
	private Panel generateSettingsTab() {
		//maybe use CheckBoxModel
		ISettings settings = SettingsManager.load();
		Container c = new Container();
		
		checkboxes = new HashMap<>();
		
		c.addChild(new Label("Rand Colours"));
		Checkbox cb = c.addChild(new Checkbox(SettingsManager.Key.RandomColours.name()), 1);
		cb.setChecked(settings.randomColours());
		cb.addClickCommands((chk -> saveSettings()));
		checkboxes.put(SettingsManager.Key.RandomColours, cb);
		
		c.addChild(new Label("Grey Scale"));
		cb = c.addChild(new Checkbox(SettingsManager.Key.GreyScale.name()), 1);
		cb.setChecked(settings.greyScale());
		cb.addClickCommands((chk -> saveSettings()));
		checkboxes.put(SettingsManager.Key.GreyScale, cb);
		
		c.addChild(new Label("Ghost Shape"));
		cb = c.addChild(new Checkbox(SettingsManager.Key.Ghost.name()), 1);
		cb.setChecked(settings.ghost());
		cb.addClickCommands((chk -> saveSettings()));
		checkboxes.put(SettingsManager.Key.Ghost, cb);
		
		c.addChild(new Label("Hard Drop Lock"));
		cb = c.addChild(new Checkbox(SettingsManager.Key.HardDropLock.name()), 1);
		cb.setChecked(settings.hardDropLock());
		cb.addClickCommands((chk -> saveSettings()));
		checkboxes.put(SettingsManager.Key.HardDropLock, cb);
		
		c.addChild(new Label("Expert Mode"));
		cb = c.addChild(new Checkbox(SettingsManager.Key.ExpertMode.name()), 1);
		cb.setChecked(settings.expertMode());
		cb.addClickCommands((chk -> saveSettings()));
		checkboxes.put(SettingsManager.Key.ExpertMode, cb);

		return c;
	}
	
	private void triggerSomething() {
		this.main.startPlay();
	}
	
	private void saveSettings() {
		HashMap<SettingsManager.Key, Object> settings = new HashMap<>();
		for (Entry<Key, Checkbox> box: checkboxes.entrySet()) {
			settings.put(box.getKey(), box.getValue().isChecked());
		}
		
		SettingsManager.save(settings);
	}
	
	@Override
	protected void cleanup(Application app) {
		((SimpleApplication)app).getGuiNode().detachChild(rootNode);
	}

	@Override
	protected void onEnable() {}
	@Override
	protected void onDisable() {}
}
