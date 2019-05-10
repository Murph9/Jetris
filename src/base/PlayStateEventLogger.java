package base;

import java.util.function.Consumer;

import logic.TetrisEventListener;

public class PlayStateEventLogger implements TetrisEventListener {

	private final Consumer<String> func;

	public PlayStateEventLogger(Consumer<String> func) {
		this.func = func;
	}
	
	@Override
	public void onNewLine(int count, boolean isB2B) {
		final String b2bStr = isB2B ? "B2B " : "";
		if (count == 1)
			this.func.accept(b2bStr+"Single");
		if (count == 2)
			this.func.accept(b2bStr+"Double");
		if (count == 3)
			this.func.accept(b2bStr+"Triple");
		if (count == 4)
			this.func.accept(b2bStr+"Tetris");
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
	
	@Override
	public void onTSpin(int count, boolean mini, boolean isB2B) {
		String str = isB2B ? "B2B " : "";
		str += mini ? "Mini " : "";
		if (count == 0)
			this.func.accept(str+"TSpin");
		if (count == 1)
			this.func.accept(str+"TSpin Single!");
		if (count == 2)
			this.func.accept(str+"TSpin Double!!");
		if (count == 3)
			this.func.accept(str+"TSpin Triple!!!");
	}
}
