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
		//clockwise
		keyLayout.put(KeyInput.KEY_UP, "RotateRight");
		keyLayout.put(KeyInput.KEY_X, "RotateRight"); 
		
		keyLayout.put(KeyInput.KEY_SPACE, "HardDrop");
		
		keyLayout.put(KeyInput.KEY_LSHIFT, "Hold");
		keyLayout.put(KeyInput.KEY_RSHIFT, "Hold");
		keyLayout.put(KeyInput.KEY_C, "Hold");
		
		//counter-clockwise
		keyLayout.put(KeyInput.KEY_RCONTROL, "RotateLeft");
		keyLayout.put(KeyInput.KEY_LCONTROL, "RotateLeft");
		keyLayout.put(KeyInput.KEY_Z, "RotateLeft");
	
		keyLayout.put(KeyInput.KEY_LEFT, "Left");
		keyLayout.put(KeyInput.KEY_A, "Left");
		
		keyLayout.put(KeyInput.KEY_RIGHT, "Right");
		keyLayout.put(KeyInput.KEY_D, "Right");

		keyLayout.put(KeyInput.KEY_P, "Pause");
		keyLayout.put(KeyInput.KEY_ESCAPE, "Pause");
		keyLayout.put(KeyInput.KEY_F1, "Pause");
		
		keyLayout.put(KeyInput.KEY_DOWN, "Drop");
		
		
		//numpad keys (untested)
		keyLayout.put(KeyInput.KEY_NUMPAD1, "RotateRight");
		keyLayout.put(KeyInput.KEY_NUMPAD5, "RotateRight");
		keyLayout.put(KeyInput.KEY_NUMPAD9, "RotateRight");
		
		keyLayout.put(KeyInput.KEY_NUMPAD8, "HardDrop");
		
		keyLayout.put(KeyInput.KEY_NUMPAD0, "Hold");
		
		keyLayout.put(KeyInput.KEY_NUMPAD3, "RotateLeft");
		keyLayout.put(KeyInput.KEY_NUMPAD7, "RotateLeft");
		
		//no pause on numpad
		
		keyLayout.put(KeyInput.KEY_NUMPAD4, "Left");
		keyLayout.put(KeyInput.KEY_NUMPAD6, "Right");
		
		keyLayout.put(KeyInput.KEY_NUMPAD2, "Drop");
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
