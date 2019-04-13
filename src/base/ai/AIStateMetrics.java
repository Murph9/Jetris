package base.ai;

import base.AiPlayState;
import base.PlayState;
import logic.Tetris;
import logic.TetrisGame;

public class AIStateMetrics implements AI {
	
	enum AiState {
		Deciding,
		FoundMove,
		Moved;
	}
	
	private final float placeDelay;
	
	private AiState state = AiState.Deciding;
	private float delayTimer;
	private int foundMoveIndex = -1;
	private int foundMoveRotation = -1;
	
	public AIStateMetrics (float placeDelay) {
		this.placeDelay = placeDelay;
	}

	@Override
	public void update(Tetris engine, float tpf) {
		//use the fancy clone method to 'dry run' moves to test for the best action
		//does state stuff until it decides that thats okay
		if (state == AiState.Deciding && engine.getLines().isEmpty()) //no moving while new line thingo is active
			decidePos((TetrisGame) engine);
		
		//hack to slow the ai down so its good as 
		this.delayTimer += tpf;
		if (this.state != AiState.Deciding && this.delayTimer > placeDelay) {
			
			switch (this.state) {
				case FoundMove:
					//move to the decided location
					for (int i = 0; i < this.foundMoveRotation; i++)
						engine.rotate(true);
				
					//we start on the left
					for (int i = 0; i < PlayState.X_SIZE; i++)
						engine.moveSide(true);

					//then move right to the spot
					for (int i = 0; i < this.foundMoveIndex; i++)
						engine.moveSide(false);
					this.state = AiState.Moved;
					break;
				case Moved:
					//and place the piece
					engine.hardDown();
					this.state = AiState.Deciding;
					this.foundMoveIndex = -1;
					this.foundMoveRotation = -1;
					break;
				default:
					break;
			}
			this.delayTimer = 0;
		}
	}

	private void decidePos(TetrisGame engine) {
		//expensive method which basically creates its own game to test which move is the best

		float bestCost = Float.MAX_VALUE;
		int bestCol = -1;
		int bestRot = -1;

		int rot = 0;
		int col = 0;
		
		while (rot < 4) {
			while (col < PlayState.X_SIZE) {
				TetrisGame tg = engine.cloneForAI();
				//rotate
				for (int i = 0; i < rot; i++)
					tg.rotate(true);

				//move full left
				for (int i = 0; i < PlayState.X_SIZE; i++) //how many moves to go across the whole screen ?? [it changes per piece type and field]
					tg.moveSide(true);

				//move right
				for (int i = 0; i < col; i++)
					tg.moveSide(false);
				
				tg.hardDown(); //place so we can evaluate it
				tg.triggerLineEnd(); //finish any line placing to evaluate them better

				float cost = evaluteState(tg);
				if (cost < bestCost) {
					bestCol = col;
					bestRot = rot;
					bestCost = cost;
				}
				col++;
			}
			col = 0;
			rot++;
		}

		this.foundMoveIndex = bestCol;
		this.foundMoveRotation = bestRot;
		System.out.print(engine.curShapeType() + " @ " + bestCost + "(prev:"+evaluteState(engine)+") => c" + bestCol + " r" + bestRot);
		System.out.println(" ||| lc" + engine.getLinesCount() + " hc" + AiPlayState.holeCount(engine) + " bi" + AiPlayState.bumpiness(engine) + " ah" + AiPlayState.aggregateHeight(engine));

		this.state = AiState.FoundMove;
	}

	//lower is better
	private float evaluteState(TetrisGame tg) {
		return -tg.getLinesCount() + 100*AiPlayState.holeCount(tg) + 20*AiPlayState.bumpiness(tg) + AiPlayState.aggregateHeight(tg);
	}
}