package logic;

public interface TetrisEventListener {

	void onNewLine(int count);
	void onLineCombo(int count);
	void onTSpin(int count);
	
	void onGameOver();
	
	void onRotation();
	void onMovement();
	void onNotMove();
	
	void onStartLockDelay();
	void onLock();
}
