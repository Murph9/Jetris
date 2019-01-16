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

public class MenuState extends BaseAppState {

	//TODO settings
	private Node rootNode;
	private Main main;
	
	public MenuState(Main m) {
		main = m;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize(Application app) {
		//init gui
		//info window first so the event listeners can delete it
		this.rootNode = new Node("menu node");
		((SimpleApplication)app).getGuiNode().attachChild(rootNode);
		
		Container myWindow = new Container();
		rootNode.attachChild(myWindow);
		myWindow.setLocalTranslation(screenTopLeft());
		myWindow.addChild(new Label("Choose Map"), 0, 0);

		Button typeA = myWindow.addChild(new Button("Start"), 1, 0);
		typeA.addClickCommands(new Command<Button>() {
            @Override
            public void execute( Button source ) {
            	triggerSomething();
            }
        });
		
		addLeaderboard();
	}
	
	private void addLeaderboard() {
		//TODO typeA only for now
		
		Container myWindow = new Container();
		rootNode.attachChild(myWindow);
		myWindow.setLocalTranslation(screenMiddle());
		myWindow.addChild(new Label("Leaderboard:"), 0, 0);
		
		myWindow.addChild(new Label("Score"), 1, 0);
		myWindow.addChild(new Label("Lines"), 1, 1);
	
		List<Record> records = RecordManager.getRecords("A", 0);
		for (int i = 0; i < Math.min(records.size(), 10); i++) {
			Record r = records.get(i);
			myWindow.addChild(new Label(r.getScore() + ""), 2 + i, 0);
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
	
	private static Vector3f screenTopLeft() {
		return new Vector3f(0, Main.CURRENT.getViewPort().getCamera().getHeight(), 0);
	}
	private static Vector3f screenBottomRight() {
		return new Vector3f(Main.CURRENT.getViewPort().getCamera().getWidth(), Main.CURRENT.getViewPort().getCamera().getHeight(), 0);
	}
	public static Vector3f screenMiddle() {
		return screenBottomRight().mult(0.5f);
	}
}
