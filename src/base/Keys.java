package base;

import java.util.HashMap;

import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;

public class Keys implements RawInputListener {

	private HashMap<Integer, String> keyLayout = new HashMap<Integer, String>();
	
	private final PlayState game;
	
	public Keys(PlayState game) {
		this.game = game;
		
		initKeys();
	}
	

	private void initKeys() {
		keyLayout.put(KeyInput.KEY_SPACE, "HardDrop");
		
		keyLayout.put(KeyInput.KEY_DOWN, "Drop");
		keyLayout.put(KeyInput.KEY_S, "Drop");
	
		keyLayout.put(KeyInput.KEY_LEFT, "Left");
		keyLayout.put(KeyInput.KEY_A, "Left");
		
		keyLayout.put(KeyInput.KEY_RIGHT, "Right");
		keyLayout.put(KeyInput.KEY_D, "Right");

		keyLayout.put(KeyInput.KEY_Z, "RotateLeft");
		keyLayout.put(KeyInput.KEY_X, "RotateRight");

		keyLayout.put(KeyInput.KEY_P, "Pause");
		keyLayout.put(KeyInput.KEY_ESCAPE, "Pause");
	}


	@Override
	public void beginInput() {}
	@Override
	public void endInput() {}
	@Override
	public void onJoyAxisEvent(JoyAxisEvent evt) {}
	@Override
	public void onJoyButtonEvent(JoyButtonEvent evt) {}
	@Override
	public void onMouseMotionEvent(MouseMotionEvent evt) {}
	@Override
	public void onMouseButtonEvent(MouseButtonEvent evt) {}
	@Override
	public void onKeyEvent(KeyInputEvent arg0) {
		if (keyLayout.containsKey(arg0.getKeyCode())) {
			game.doAction(keyLayout.get(arg0.getKeyCode()), arg0.isPressed(), arg0.isPressed() ? 1 : 0);
		}
	}
	@Override
	public void onTouchEvent(TouchEvent evt) {}
}
