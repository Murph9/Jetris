package logic;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TetrisGame implements Tetris {
	
	//frame count per level (at 60 fps)
	private static int[] SPEED_LIST = {53,49,45,41,37,33,28,22,17,11,10,9,8,7,6,6,5,5,4,4,3};
	
	private GameType type;
	
	private Shape curShape;
	private Shape nextShape;
	private Shape ghostShape;
	
	private int width;
	private int height;
	private Cell[][] cells;
	
	private int level;
	private int score;
	private int lines;
	public int getScore() { return score; }
	public int getLinesCount() { return lines; }
	public int getLevel() { return level; }
	
	private int softCount; //as in soft drop counter (technical term for pressing down lots)
	private float dropTimer; //in milliseconds
	
	private final LinkedList<Integer> flashRows;
	/**
	 * Check this method to see if the 'new' line view stuff should trigger, we buffer any inputs in this mode.
	 * @return If there is a new line.
	 */
	public boolean newLine() { return !flashRows.isEmpty(); }
	public List<Integer> getLines() { return new LinkedList<Integer>(flashRows); }

	private boolean ended;
	public boolean isGameOver() { return ended; }
	
	public TetrisGame(int width, int height) {
		this.width = width;
		this.height = height;
		this.flashRows = new LinkedList<Integer>();
		
		this.cells = new Cell[this.height][this.width];
		
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				this.cells[y][x] = new Cell(x, y);
			}
		}
	}
	
	public void initialise(GameType type, int lines) {
		this.type = type;
		
		this.curShape = Shape.GenerateRand();
		this.nextShape = Shape.GenerateRand();
		updateGhostShape();
		
		if (type == GameType.TYPE_B) {
			//generate the random lines for type B
			Random randomNumGen = new Random();
			int cellsCount = lines*5;

			for (int i=0; i <cellsCount; i++) {
				int tempX = (randomNumGen.nextInt(this.width));
				int tempY = (randomNumGen.nextInt(lines) + this.height - lines);
				if (isCellFilled(tempX, tempY)) {
					i--;
				} else { //place :D
					fillCell(tempX, tempY, new CellColour(0,0,0,1));
				}
			}
		}
		
		resetDropTimer();
	}
	
	public void update(float tpf) {
		if (this.ended)
			return; //no more game updates now :(
		
		if (newLine())
			return; //do not do anything until the view layer says its done 
		
		this.dropTimer -= tpf;
		if (dropTimer <= 0) {
			timerDown();
			resetDropTimer();
		}
	}
	private void resetDropTimer() {
		this.dropTimer = (SPEED_LIST[this.level]/60f);
	}
	
	public LinkedList<Cell> curShapeCells() {
		return new LinkedList<>(this.curShape.shapeCells);
	}
	public LinkedList<Cell> nextShapeCells() {
		return new LinkedList<>(this.nextShape.shapeCells);
	}
	public LinkedList<Cell> ghostShapeCells() {
		return new LinkedList<>(this.ghostShape.shapeCells);
	}
	
	private void updateGhostShape() {
		//TODO this method is a slow clone and move, it could be smarter
		this.ghostShape = this.curShape.clone();
		this.ghostShape.translate(0, minDrop() - 1);
	}
	

	private void timerDown() {
		this.softCount = 0;
		if (curShape == null || this.ended)
			return;
		
		Shape newState = this.curShape.clone();
		newState.translate(0, 1);
		
		if (!isValidMove(newState)) {
			blockHit(); //a down move failed, place in world
			return;
		}
		
		this.curShape = newState;
		updateGhostShape();
	}
	
	
	@Override
	public void hardDown() {
		this.softCount = 0;
		if (curShape == null || this.ended)
			return;
		
		this.softCount = minDrop() - 1;
		
		this.curShape.translate(0, this.softCount);
		
		this.softCount *= 2; //hard drop gets double points
		
		blockHit();
	}
	
	@Override
	public void softDown() {
		if (curShape == null || this.ended)
			return;
		
		Shape newState = this.curShape.clone();
		newState.translate(0, 1);
		
		if (!isValidMove(newState)) {
			blockHit(); //a down move failed, place in world
			return;
		}
		
		this.softCount++;
		this.curShape = newState;
	}
	
	@Override
	public void moveSide(boolean left) {
		if (curShape == null || this.ended)
			return;
		//could return a value if the state changed
		
		Shape newState = this.curShape.clone();
		newState.translate(left ? -1 : 1, 0);
		
		if (!isValidMove(newState))
			return;
		
		this.curShape = newState;
		updateGhostShape();
	}

	@Override
	public void rotate(boolean left) {
		if (curShape == null || this.ended)
			return;

		Shape newState = this.curShape.clone();
		newState = ShapeRotator.rotate(newState, left, (s) -> {
			return isValidMove(s);
		});
		
		if (!isValidMove(newState))
			return;
		
		this.curShape = newState;
		updateGhostShape();
	}
	
	
	private boolean isValidMove(Shape nextState) {
		for (Cell c: nextState.getCells()) {
			if (c.getX() >= width || c.getX() < 0) 
				return false;
			if (c.getY() >= height)
				return false;
			
			if (isCellFilled(c.getX(), c.getY()))
				return false;
		}
		return true;
	}
	
	private void blockHit() {
		//place block, spawn the next one
		
		//stop the drop timer from working
		this.dropTimer = Float.MAX_VALUE;

		for (Cell c: this.curShape.getCells()) { 
			//add the cells to the grid
			fillCell(c.getX(), c.getY(), c.getColour());
		}

		this.score += this.softCount; //pressed down quite a few times
		this.softCount = 0; //but it has to be zero for the next one

		//check for new lines
		updateFlashRows();
		if (newLine()) {
			return;
		}
		
		spawnNextBlock();
	}
	
	/**
	 * Trigger this method when the ui code is done with showing the line.
	 */
	public void triggerLineEnd() {
		this.score += computeLineScore(flashRows.size());
		this.lines += flashRows.size();
		this.level = this.lines/10;
		
		for (Integer i: flashRows)
			removeRow(i);
		
		this.flashRows.clear();

		spawnNextBlock();
	}
	
	private void spawnNextBlock() {
		//generate the next shapes
		this.curShape = this.nextShape;
		this.nextShape = Shape.GenerateRand();
		
		for (Cell c: this.curShape.getCells()) {
			if (isCellFilled(c.getX(), c.getY())) {
				this.ended = true;
				return;
			}
		}
		
		updateGhostShape();
		resetDropTimer();
	}
	
	private void fillCell(int x, int y, CellColour colour) {
		if (x < 0 || y < 0)
			return;
		cells[y][x].fill(colour);
	}
	
	private boolean isCellFilled(int x, int y) {
		if (x < 0 || x >= width) return false;
		if (y < 0 || y >= height) return false;
		return cells[y][x].getFilled();
	}
	
	public Cell getCell(int x, int y) {
		if (x < 0 || x >= width) return null;
		if (y < 0 || y >= height) return null;
		
		return cells[y][x];
	}
	
	private void updateFlashRows() {
		flashRows.clear();
		for (int y = 0; y < this.height; y++) {
			boolean full = true;
			for (int x = 0; x < this.width; x++) {
				if (!cells[y][x].getFilled()) {
					full = false;
					break;
				}
			}
			if (full == true) {
				flashRows.add(y);
			}
		}
	}
	
	private int computeLineScore(int lines) {
		int lineScore = 0;
		switch (lines) {
		case 1:
			lineScore = 40*(this.level+1);
			break;
		case 2:
			lineScore = 100*(this.level+1);
			break;
		case 3:
			lineScore = 300*(this.level+1);
			break;
		case 4:
			lineScore = 1200*(this.level+1);
			break;
		}
		return lineScore;
	}
	
	//assumes the row given is valid
	private void removeRow(int row) {
		for (int i = 0; i < this.width; i++) {
			cells[row][i].clear();
		}
		for (int y = row-1; y >= 0; y--) { //need to go up for this one
			for (int x = 0; x < this.width; x++) {
				if (cells[y][x].getFilled()) {
					cells[y+1][x].fill(cells[y][x].getColour());
					cells[y][x].clear();
				}
			}
		}
	}
	
	private int minDrop() {
		if (curShape == null) 
			return -1;
		
		int length = Integer.MAX_VALUE;
		for (Cell c : curShape.getCells()) {
			int temp = 0;
			for (int i = c.getY(); i < height; i++) {
				if (this.isCellFilled(c.getX(), i)) {
					length = Math.min(length, temp);
						//we want the smallest distance for a drop
					break;
				}
				temp++;
			}
			length = Math.min(length, temp);
		}
		return length;
	}
	
	@Override
	public String toString() {
		return this.type+" " + this.curShape.type + " " + this.score + " " + this.lines;
	}
}
