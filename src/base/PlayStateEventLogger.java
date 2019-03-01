package base;

import java.util.function.Consumer;

import logic.TetrisEventListener;

public class PlayStateEventLogger implements TetrisEventListener {

	private final Consumer<String> func;

	public PlayStateEventLogger(Consumer<String> func) {
		this.func = func;
	}
	
	@Override
	public void onNewLine(int count) {
		if (count < 2)
			this.func.accept(count + " line");
		else if (count == 4)
			this.func.accept("Tetris");
		else
			this.func.accept(count + " lines");
	}
	
	@Override
	public void onLineCombo(int count) {
		if (count > 1) //a combo of one is not interesting
			this.func.accept(count + " combo!");
	}

	@Override
	public void onGameOver() {
		this.func.accept("Game Over!");
	}

	@Override
	public void onRotation() {
		
	}

	@Override
	public void onMovement() {
		
	}

	@Override
	public void onNotMove() {
		
	}

	@Override
	public void onStartLockDelay() {
		
	}
	
	@Override
	public void onLock() {
		
	}
}
