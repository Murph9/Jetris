package jetris.base.ai;

import jetris.logic.Tetris;
import jetris.logic.TetrisGame;

public class AIBasicHardDown implements AI {
	//attempt1 - really hacky slow way:
	//move to every start pos and check hard drop distance, pick the smallest one for all rotations

	//slow the ai down a little
	private static final float DELAY = 1f;
	private float delayTimer;
	
	private int foundMoveIndex = -1;
	private int foundMoveRotation = -1;

	@Override
	public void update(Tetris engine, float tpf) {
		delayTimer += tpf;

		if (this.foundMoveIndex == -1) {
			TetrisGame tg = ((TetrisGame)engine).cloneForAI();

			int maxValue = 0;
			for (int rot = 0; rot < 4; rot++) {

				for (int i = 0; i < 7; i++)
					tg.moveSide(true);
				
				for (int i = 0; i < 20; i++) { //how many moves to go across the whole screen ?? [it changes per piece type at least]
					int cur = tg.minDrop();
					if (cur > maxValue) {
						maxValue = cur;
						this.foundMoveIndex = i;
						this.foundMoveRotation = rot;
					}
					tg.moveSide(false);
				}

				tg.rotate(true);
			}
		}

		if (this.foundMoveIndex != -1 && this.delayTimer > DELAY) {
			for (int i = 0; i < this.foundMoveRotation; i++)
				engine.rotate(true);

			for (int i = 0; i < 20; i++) //we start on the left
				engine.moveSide(true);

			for (int i = 0; i < this.foundMoveIndex; i++)  {
				engine.moveSide(false);
			}
			engine.hardDown();
			
			this.delayTimer = 0;
			this.foundMoveIndex = -1;
			this.foundMoveRotation = -1;
		}
	}
}
