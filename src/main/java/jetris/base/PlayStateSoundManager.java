package jetris.base;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;

import jetris.logic.TetrisEventListener;
import jetris.logic.Tetris;

public class PlayStateSoundManager extends BaseAppState {

	private TetrisEventListener soundy;
	private Node rootNode;
	private final Tetris engine;
	
	private AudioNode backgroundMusic;
	
	public PlayStateSoundManager(Tetris engine) {
		this.engine = engine;
		
		this.rootNode = new Node("Sound rootNode");
	}
	
	@Override
	protected void initialize(Application app) {
		((SimpleApplication)app).getRootNode().attachChild(rootNode);
		soundy = new SoundListener(rootNode, app.getAssetManager());
		engine.addEventListener(soundy);
		
		//add looping background sound
		backgroundMusic = new AudioNode(app.getAssetManager(), "sounds/jetris_theme-03.wav", DataType.Stream);
		backgroundMusic.setLooping(true);
		backgroundMusic.setPositional(false);
		rootNode.attachChild(backgroundMusic);
	}

	@Override
	protected void cleanup(Application app) {
		((Main)app).getRootNode().detachChild(rootNode);
		engine.removeEventListener(soundy);
		
		backgroundMusic.stop();
		rootNode.detachChild(backgroundMusic);
	}

	@Override
	protected void onEnable() {
		if (backgroundMusic != null)
			backgroundMusic.play();
	}

	@Override
	protected void onDisable() {
		if (backgroundMusic != null)
			backgroundMusic.pause();
	}
	
	class SoundListener implements TetrisEventListener {

		private static final String BOP = "sounds/bop.wav";
		private static final String BOP_LOW = "sounds/bop_low.wav";
		private static final String BOP_HIGH = "sounds/bop_high.wav";
		private static final String EXP = "sounds/exp.wav";
		private static final String LOCK = "sounds/lock.wav";
		private static final String RAND = "sounds/rand.wav";
		
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
		public void onNewLine(int count, boolean isB2b) {
			newLine.play();
		}
		
		@Override
		public void onLineCombo(int count) {
			//no idea yet
		}
		
		@Override
		public void onTSpin(int count, boolean mini, boolean isB2b) {
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
