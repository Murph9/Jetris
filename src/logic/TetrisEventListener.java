package logic;

public interface TetrisEventListener {

	void onNewLine(int count, boolean b2b);
	void onTSpin(int count, boolean b2b);
	void onLineCombo(int count);
	
	void onGameOver();
	
	void onRotation();
	void onMovement();
	void onNotMove();
	
	void onStartLockDelay();
	void onLock();
}
