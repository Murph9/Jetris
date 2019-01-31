package base;

import com.jme3.app.Application;

public class AiPlayState extends PlayState {

	public AiPlayState(Main m) {
		super(m);
	}
	//hopefully this class's goal is obvious by the class name
	//Idea is to 'watch' it play on button press
	
	@Override
	public void initialize(Application app) {
		super.initialize(app);
		
		app.getInputManager().removeRawInputListener(keys);
	}
	
	class AI {
		//TODO place it as low as possible without holes, no other goals
	}
}
