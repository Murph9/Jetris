package base;

import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BorderLayout.Position;

public class MenuState extends BaseAppState {

	//TODO settings
	//TODO info page
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
		
		Container myWindow = new Container();
		mainWindow.addChild(myWindow, Position.Center);
		myWindow.addChild(new Label("Leaderboard:"), 0, 0);
		
		myWindow.addChild(new Label("Score"), 1, 0);
		myWindow.addChild(new Label("Lines"), 1, 1);
	
		List<Record> records = RecordManager.getRecords("A", 0);
		for (int i = 0; i < Math.min(records.size(), 10); i++) {
			Record r = records.get(i);
			myWindow.addChild(new Label(r.getScore() + (r.isNew() ? "*" : "")), 2 + i, 0);
			myWindow.addChild(new Label(r.getLineCount() + ""), 2 + i, 1);
		}
	}

	private void triggerSomething() {
		this.main.startPlay();
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
