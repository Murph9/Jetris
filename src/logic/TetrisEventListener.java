package logic;

public interface TetrisEventListener {

	void onNewLine();
	void onGameOver();
	
	void onRotation();
	void onMovement();
	void onNotMove();
	
	void onStartLockDelay();
	void onLock();
}
