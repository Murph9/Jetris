package base;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;

import logic.TetrisEventListener;
import logic.Tetris;

public class PlayStateSoundManager extends BaseAppState {

	private TetrisEventListener soundy;
	private Node rootNode;
	private final Tetris engine;
	
	public PlayStateSoundManager(Tetris engine) {
		this.engine = engine;
		
		this.rootNode = new Node("Sound rootNode");
	}
	
	@Override
	protected void initialize(Application app) {
		((SimpleApplication)app).getRootNode().attachChild(rootNode);
		soundy = new SoundListener(rootNode, app.getAssetManager());
		engine.addEventListener(soundy);
	}

	@Override
	protected void cleanup(Application app) {
		((Main)app).getRootNode().detachChild(rootNode);
		engine.removeEventListener(soundy);
	}

	@Override
	protected void onEnable() {}

	@Override
	protected void onDisable() {}
	
	class SoundListener implements TetrisEventListener {

		private static final String BOP = "assets/sounds/bop.wav";
		private static final String BOP_LOW = "assets/sounds/bop_low.wav";
		private static final String BOP_HIGH = "assets/sounds/bop_high.wav";
		private static final String EXP = "assets/sounds/exp.wav";
		private static final String LOCK = "assets/sounds/lock.wav";
		private static final String RAND = "assets/sounds/rand.wav";
		
		private AudioNode newLine;
		private AudioNode gameOver;
		
		private AudioNode rotation;
		private AudioNode movement;
		private AudioNode notMove;
		private AudioNode lockDelay;
		private AudioNode lock;

		public SoundListener(Node rootNode, AssetManager am) {
			newLine = new AudioNode(am, EXP, DataType.Buffer);
		    newLine.setPositional(false);
		    newLine.setLooping(false);
		    rootNode.attachChild(newLine);
		    
		    gameOver = new AudioNode(am, RAND, DataType.Buffer);
		    gameOver.setPositional(false);
		    gameOver.setLooping(false);
		    rootNode.attachChild(gameOver);
		    
		    rotation = new AudioNode(am, BOP_HIGH, DataType.Buffer);
		    rotation.setPositional(false);
		    rotation.setLooping(false);
		    rootNode.attachChild(rotation);
		    
		    movement = new AudioNode(am, BOP, DataType.Buffer);
		    movement.setPositional(false);
		    movement.setLooping(false);
		    rootNode.attachChild(movement);
		    
		    notMove = new AudioNode(am, BOP_LOW, DataType.Buffer);
		    notMove.setPositional(false);
		    notMove.setLooping(false);
		    rootNode.attachChild(notMove);
		    
		    lockDelay = new AudioNode(am, LOCK, DataType.Buffer); //TODO new sound pls
		    lockDelay.setPositional(false);
		    lockDelay.setLooping(false);
		    rootNode.attachChild(lockDelay);
		    
		    lock = new AudioNode(am, LOCK, DataType.Buffer);
		    lock.setPositional(false);
		    lock.setLooping(false);
		    rootNode.attachChild(lock);
		}
		
		@Override
		public void onNewLine() {
			newLine.play();
		}

		@Override
		public void onGameOver() {
			gameOver.play();
		}

		@Override
		public void onRotation() {
			rotation.play();
		}

		@Override
		public void onMovement() {
			movement.play();
		}

		@Override
		public void onNotMove() {
			notMove.play();
		}

		@Override
		public void onStartLockDelay() {
			lockDelay.play();
		}
		
		@Override
		public void onLock() {
			lock.play();
		}
	}
}
