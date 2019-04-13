package base;

import com.jme3.app.Application;

import base.ai.AI;
import base.ai.AIStateMetrics;
import logic.TetrisGame;

public class AiPlayState extends PlayState {

	private AI ai;

	public AiPlayState(Main m) {
		super(m);

		this.ai = new AIStateMetrics(0.5f);
	}
	
	//hopefully this class's goal is obvious by the class name
	//Idea is to 'watch' it play on the main menu
	
	@Override
	public void initialize(Application app) {
		super.initialize(app);
		
		app.getInputManager().removeRawInputListener(keys);
	}
	
	@Override
	public void update(float tpf) {
		super.update(tpf);

		ai.update(this.engine, tpf);
	}


	//some helper methods to calculate AI values
	//all value ideas taken from:
	//https://codemyroad.wordpress.com/2013/04/14/tetris-ai-the-near-perfect-player/

	public static int columnCount(TetrisGame engine, int column) {
		//count the filled cells
		int count = 0;
		for (int i = 0; i < PlayState.Y_SIZE; i++)
			if (engine.getCell(column, i).getFilled())
				count++;

		return count;
	}

	public static int columnHeight(TetrisGame engine, int column) {
		//find the highest cell
		for (int i = PlayState.Y_SIZE - 1; i >= 0; i--) {
			if (engine.getCell(column, i).getFilled())
				return i;
		}
		return 0;
	}
	//cumulative game static height
	public static int aggregateHeight(TetrisGame engine) {
		int sum = 0;
		for (int i = 0; i < PlayState.X_SIZE; i++) {
			sum += columnHeight(engine, i);
		}
		return sum;
	}

	//counts how many holes on the field
	public static int holeCount(TetrisGame engine) {
		int count = 0;
		for (int c = 0; c < PlayState.X_SIZE; c++){
			boolean block = false;
			for (int r = PlayState.Y_SIZE - 1; r >= 0; r--) {
				if (engine.getCell(c, r).getFilled()) {
					block = true;
				} else if (!engine.getCell(c, r).getFilled() && block) {
					count++;
				}
			}
		}
		return count;
	}

	//value to estimate how level the play field is
	public static int bumpiness(TetrisGame engine) {
		int last = 0;

		int bumpiness = last;
		for (int i = 0; i < PlayState.X_SIZE; i++) {
			int cur = columnHeight(engine, i);
			bumpiness += Math.abs(last - cur);
			last = cur;
		}
		return bumpiness;
	}
}
