package logic;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TetrisGame implements Tetris {
	
	private final float LOCK_DELAY = 0.5f; //feels weird, like it should get less per level
	
	//keep hidden due to GRAVITY_DOWN (that should not be public)
	private enum InputAction {
		HARD_DOWN, GRAVITY_DOWN, SOFT_DOWN, MOVE_LEFT, MOVE_RIGHT, ROTATE_LEFT, ROTATE_RIGHT, HOLD;
	}
	
	private final LogicSettings settings;
	
	private final Generator shapeGenerator;
	private final Shape[] nextShapes;
	private Shape curShape;
	private Shape ghostShape;
	private Shape holdShape;
	
	private final int width;
	private final int height;
	private final Cell[][] cells;
	
	private int level;
	private int levelPoints;
	
	private int score;
	private int lines;
	public int getScore() { return score; }
	public int getLinesCount() { return lines; }
	public int getLevel() { return level; }
	
	private int softCount; //as in soft drop counter (technical term for pressing down lots)
	private float dropTimer; //in sec
	private float lockTimer; //in sec
	private boolean pieceHeld; //to set if hold was pressed, to prevent pressing it again
	
	private final LinkedList<Integer> flashRows;
	/**
	 * Check this method to see if the 'new' line view stuff should trigger, we buffer any inputs in this mode.
	 * @return If there is a new line.
	 */
	public boolean newLine() { return !flashRows.isEmpty(); }
	public List<Integer> getLines() { return new LinkedList<Integer>(flashRows); }

	private boolean ended;
	public boolean isGameOver() { return ended; }
	
	public TetrisGame(int width, int height, int nextShapes, LogicSettings settings) {
		this.settings = settings; //TODO null check
		this.width = width;
		this.height = height;
		this.flashRows = new LinkedList<Integer>();
		this.shapeGenerator = new Generator(Shape.Type.values());
		this.nextShapes = new Shape[nextShapes];
		
		this.level = 1;
		
		this.cells = new Cell[this.height][this.width];
		
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				this.cells[y][x] = new Cell(x, y);
			}
		}
	}
	
	public void initialise() {
		this.curShape = shapeGenerator.next();
		for (int i = 0; i < this.nextShapes.length; i++) {
			this.nextShapes[i] = shapeGenerator.next();
		}
		
		//type B has been removed, look at source history for info
		
		updateGhostShape();
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
		
		if (this.lockTimer > 0) {
			this.lockTimer -= tpf;
		} else if (this.lockTimer < 0) {
			//lock timer done, so lock it in
			blockHit();
			this.lockTimer = 0;
		}
	}
	private void resetDropTimer() {
		this.dropTimer = gravityCalc(this.level);
	}
	/** get gravity by level in seconds */
	private static float gravityCalc(int level) {
		//TODO this needs to rounded to the nearest frame (its currently one frame longer)
		return (float) Math.pow(0.8f-((level-1)*0.007f), level-1);
	}
	
	public List<Cell> curShapeCells() {
		return new LinkedList<>(this.curShape.shapeCells);
	}
	public List<Cell> nextShapeCells(int i) {
		return new LinkedList<>(this.nextShapes[i].shapeCells);
	}
	public List<Cell> ghostShapeCells() {
		return new LinkedList<>(this.ghostShape.shapeCells);
	}
	public List<Cell> holdShapeCells() {
		if (holdShape == null)
			return Collections.<Cell>emptyList();
		return new LinkedList<>(this.holdShape.shapeCells);
	}
	
	private void updateGhostShape() {
		this.ghostShape = this.curShape.clone();
		this.ghostShape.translate(0, minDrop() - 1);
	}
	
	public void hold() { movePiece(InputAction.HOLD); }
	public void hardDown() { movePiece(InputAction.HARD_DOWN); }
	public void softDown() { movePiece(InputAction.SOFT_DOWN); }
	private void timerDown() { movePiece(InputAction.GRAVITY_DOWN); }
	public void moveSide(boolean left) {
		if (left)
			movePiece(InputAction.MOVE_LEFT);
		else
			movePiece(InputAction.MOVE_RIGHT);
	}
	public void rotate(boolean right) {
		if (right)
			movePiece(InputAction.ROTATE_RIGHT);
		else
			movePiece(InputAction.ROTATE_LEFT);
	}
	
	private void movePiece(InputAction action) {
		if (curShape == null || this.ended)
			return; 
		if (this.newLine())
			return; //TODO add ignore/buffer during line show mode
		
		Shape newState = this.curShape.clone();
		switch (action) {
		case HARD_DOWN:
			//hard down is special, if always works and always causes the block to lock
			this.softCount = minDrop() - 1;
			newState.translate(0, this.softCount);
			this.softCount *= 2; //hard drop gets double points
			this.curShape = newState;
			if (settings.hardDropLock) { //TODO basically forfeits and softlock points 
				blockHit();
				return;
			}
		case HOLD:
			this.softCount = 0;
			if (pieceHeld)
				return;
			pieceHeld = true;
			
			//hold case is special, if hold empty move to hold and go to the next one
			if (this.holdShape == null) {
				this.holdShape = Shape.getNew(newState.type); //reset position
				spawnNextBlock();
				return;
			}
			//else swap with current (reset position)
			Shape.Type holdType = this.curShape.type;
			this.curShape = Shape.getNew(this.holdShape.type); //reset position
			this.holdShape = Shape.getNew(holdType); //reset position
			updateGhostShape();
			return;
		case SOFT_DOWN:
			newState.translate(0, 1);
			this.softCount++;
			resetDropTimer(); //reset gravity timer
			break;
		case GRAVITY_DOWN:
			newState.translate(0, 1);
			this.softCount = 0;
			break;
		case MOVE_LEFT:
			newState.translate(-1, 0);
			this.softCount = 0;
			break;
		case MOVE_RIGHT:
			newState.translate(1, 0);
			this.softCount = 0;
			break;
		case ROTATE_LEFT:
			newState = ShapeRotator.rotate(newState, true, (s) -> {
				return isValidMove(s);
			});
			this.softCount = 0;
			break;
		case ROTATE_RIGHT:
			newState = ShapeRotator.rotate(newState, false, (s) -> {
				return isValidMove(s);
			});
			this.softCount = 0;
			break;
		}
		
		if (!isValidMove(newState)) {
			if (this.lockTimer == 0 && (action == InputAction.SOFT_DOWN || action == InputAction.GRAVITY_DOWN || action == InputAction.HARD_DOWN)) {
				//a down move failed, trigger lock delay
				this.lockTimer = LOCK_DELAY;
			}
			return;
		}
		
		//valid move, so update curShape
		this.curShape = newState;
		updateGhostShape();
		this.lockTimer = 0; //any valid move resets the lock timer
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
		pieceHeld = false;
		
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
		updateByLines(flashRows.size());
		
		for (Integer i: flashRows)
			removeRow(i);
		
		this.flashRows.clear();

		spawnNextBlock();
	}
	
	private void spawnNextBlock() {
		//generate the next shapes
		this.curShape = this.nextShapes[0];
		for (int i = 1; i < this.nextShapes.length; i++) {
			this.nextShapes[i-1] = this.nextShapes[i];
		}
		this.nextShapes[this.nextShapes.length - 1] = shapeGenerator.next();
		
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
	
	private void updateByLines(int lines) {
		this.lines += lines;
		
		//http://tetris.wikia.com/wiki/Scoring#Guideline_scoring_system
		switch (lines) {
		case 1: //single
			this.score += 100*this.level;
			this.levelPoints += 1;
			break;
		case 2: //double
			this.score += 100*this.level;
			this.levelPoints += 3;
			break;
		case 3: //triple
			this.score += 500*this.level;
			this.levelPoints += 5;
			break;
		case 4: //tetris
			this.score += 800*this.level;
			this.levelPoints += 8;
			break;
		}
		
		//update level based on levelPoints
		while (this.levelPoints > this.level*5) {
			this.levelPoints -= this.level*5;
			this.level++;
		}
		
		this.level = Math.min(this.level, 20); //prevent a higher level than 20
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
		return "Tetris: " + this.score + " " + this.lines + " " + this.curShape.type;
	}
}
