package logic;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import logic.Shape.Type;

public class TetrisGame implements Tetris {
	
	//note this for later: https://tetris.fandom.com/wiki/Infinity
	//whats funny is it mentions 'O' being able to rotate forever as well
	
	private static final float LOCK_DELAY = 0.5f; //feels weird, like it should get less per level
	
	//kept hidden due to GRAVITY_DOWN (which should not be public)
	private enum InputAction {
		HARD_DOWN, GRAVITY_DOWN, SOFT_DOWN, MOVE_LEFT, MOVE_RIGHT, ROTATE_LEFT, ROTATE_RIGHT, HOLD;
	}
	
	private final LogicSettings settings;
	
	private final Generator shapeGenerator;
	private final Shape[] nextShapes;
	private Shape curShape;
	private Shape ghostShape;
	private Shape holdShape;
	
	private CellGrid field;
	
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
	
	private int lineCombo; //for scoring, 1 per combo
	private InputAction lastSuccessfulMoveType; //for recording the last move
	
	private final LinkedList<Integer> flashRows;
	/**
	 * Check this method to see if the 'new' line view stuff should trigger, we buffer any inputs in this mode.
	 * @return If there is a new line.
	 */
	public boolean newLine() { return !flashRows.isEmpty(); }
	public List<Integer> getLines() { return new LinkedList<Integer>(flashRows); }
	
	private final LinkedList<InputAction> lineModeActionBuffer;

	private boolean ended;
	public boolean isGameOver() { return ended; }
	
	private List<TetrisEventListener> listeners;
	
	public TetrisGame(int width, int height, int nextShapes, LogicSettings settings) {
		
		this.settings = settings != null ? settings : new LogicSettings();
		
		this.field = new CellGrid(width, height);
		this.flashRows = new LinkedList<Integer>();
		this.lineModeActionBuffer = new LinkedList<>();
		this.shapeGenerator = new Generator(Shape.Type.values());
		this.nextShapes = new Shape[nextShapes];
		
		this.level = 1;
		
		this.listeners = new LinkedList<TetrisEventListener>();
	}
	
	public void addEventListener(TetrisEventListener listener) {
		this.listeners.add(listener);
	}
	public void removeEventListener(TetrisEventListener listener) {
		if (this.listeners.contains(listener))
			this.listeners.remove(listener);
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
	
	public Shape.Type curShapeType() {
		return this.curShape.type;
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
		this.ghostShape.translate(0, -field.minDrop(curShape) + 1);
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
		if (this.newLine()) {
			//add action buffer during line show mode
			lineModeActionBuffer.add(action);
			return; 
		}
		
		Shape newState = this.curShape.clone();
		switch (action) {
		case HARD_DOWN:
			//hard down is special, if always works and always causes the block to lock
			this.softCount = field.minDrop(curShape) - 1;
			newState.translate(0, -this.softCount);
			this.softCount *= 2; //hard drop gets double points
			this.curShape = newState;
			if (settings.hardDropLock) { //Note: this basically forfeits any softlock points
				this.lockTimer = 0;
				blockHit();
			}
			return;
		case HOLD:
			if (!settings.useHoldPiece)
				break; //no hold piece action allowed
			
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
			if (this.lockTimer != 0) //soft lock timer prevents this action
				return;

			newState.translate(0, -1);
			this.softCount++;
			resetDropTimer(); //reset gravity timer
			break;
		case GRAVITY_DOWN:
			newState.translate(0, -1);
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
			newState = ShapeRotator.rotate(newState, false, (s) -> {
				return field.isValidPos(s);
			});
			this.softCount = 0;
			break;
		case ROTATE_RIGHT:
			newState = ShapeRotator.rotate(newState, true, (s) -> {
				return field.isValidPos(s);
			});
			this.softCount = 0;
			break;
		}
		
		if (!field.isValidPos(newState)) {
			if (this.lockTimer == 0 && (action == InputAction.SOFT_DOWN || action == InputAction.GRAVITY_DOWN || action == InputAction.HARD_DOWN)) {
				//a down move failed, trigger lock delay
				this.lockTimer = LOCK_DELAY;
				triggerEvent(EventType.StartLockDelay);
			} else if (action != InputAction.GRAVITY_DOWN) {
				//this shouldn't play on gravity down because either it locks or triggers the lock_delay
				triggerEvent(EventType.NotMove);
			}
			return;
		}
		
		lastSuccessfulMoveType = action;
		triggerEvent(EventType.Movement);
		
		//valid move, so update curShape
		this.curShape = newState;
		updateGhostShape();
		this.lockTimer = 0; //any valid move resets the lock timer
	}
	
	public Cell getCell(int x, int y) {
		return field.getCell(x, y);
	}
	public int minDrop() {
		return field.minDrop(curShape);
	}
	
	private void blockHit() {
		//place block, spawn the next one
		pieceHeld = false;
		
		triggerEvent(EventType.Lock);
		
		//stop the drop timer from working
		this.dropTimer = Float.MAX_VALUE;
		
		//detect t-spins: 3-corner T mode
		//https://tetris.fandom.com/wiki/T-Spin
		//TODO more complex T-spin mini rules from the link
		boolean tSpin = false;
		if (this.curShape.type == Type.T && (lastSuccessfulMoveType == InputAction.ROTATE_LEFT || lastSuccessfulMoveType == InputAction.ROTATE_RIGHT)) {
			//check 3 of 4 corners are filled
			int x = (int)this.curShape.xCentre; //T's center is a whole number
			int y = (int)this.curShape.yCentre;
			int count = 0;
			if (field.isCellFilled(x - 1, y - 1))
				count++;
			if (field.isCellFilled(x + 1, y - 1))
				count++;
			if (field.isCellFilled(x - 1, y + 1))
				count++;
			if (field.isCellFilled(x + 1, y + 1))
				count++;
			
			tSpin = count >= 3;
		}

		for (Cell c: this.curShape.getCells()) { 
			//add the cells to the grid
			CellColour c2 = c.getColour();
			if (settings.invisibleLockedCells)
				c2 = null;
			field.fillCell(c.getX(), c.getY(), c2);
		}

		this.score += this.softCount; //pressed down quite a few times
		this.softCount = 0; //but it has to be zero for the next one

		
		//TopOut rules: check if the cells are all above row 20 (21 or 22) then end the game
		boolean above = true;
		for (Cell c: this.curShape.getCells()) {
			if (c.getY() < 21) {
				above = false;
			}
		}
		if (above) {
			this.ended = true;
			triggerEvent(EventType.GameOver);
			return;
		}
		
		
		//check for new lines
		updateFlashRows();
		if (newLine()) {
			updateByLines(flashRows.size(), tSpin);
			return;
		}

		if (tSpin) { //TSpin with no lines
			triggerEvent(EventType.TSpin, 0);
			this.score += 400*level; //not great that this is out here
			updateLevel();
		}
		
		this.lineCombo = 0;
		
		spawnNextBlock();
	}
	
	/**
	 * Trigger this method when the ui code is done with showing the line.
	 */
	public void triggerLineEnd() {
		field.removeRows(flashRows);
		this.flashRows.clear();

		spawnNextBlock();
		
		//then run through the list of buffered actions
		List<InputAction> actions = new LinkedList<>(this.lineModeActionBuffer);
		lineModeActionBuffer.clear(); //prevent this buffer fill from duping if causing a line
		for (InputAction a: actions) {
			this.movePiece(a);
		}
	}
	
	private void spawnNextBlock() {
		//generate the next shapes
		this.curShape = this.nextShapes[0];
		for (int i = 1; i < this.nextShapes.length; i++) {
			this.nextShapes[i-1] = this.nextShapes[i];
		}
		this.nextShapes[this.nextShapes.length - 1] = shapeGenerator.next();
		
		//check if the next piece can spawn
		for (Cell c: this.curShape.getCells()) {
			if (field.isCellFilled(c.getX(), c.getY())) {
				triggerEvent(EventType.GameOver);
				this.ended = true;
				return;
			}
		}
		
		updateGhostShape();
		resetDropTimer();
	}
	
	private void updateFlashRows() {
		//fill flash rows and check for new lines
		flashRows.clear();
		for (Integer i: field.getFullRows()) {
			flashRows.add(i);
		}
	}
	
	private void updateByLines(int lines, boolean tSpin) {
		this.lines += lines;
		this.lineCombo++;
		
		if (tSpin)
			triggerEvent(EventType.TSpin, lines);
		else
			triggerEvent(EventType.Line, lines);
		
		triggerEvent(EventType.LineCombo, lineCombo);

		//http://tetris.wikia.com/wiki/Scoring#Guideline_scoring_system
		
		this.score += 50*lineCombo*level;

		if (tSpin && lines == 3) { //T-Spin Triple
			this.score += 1600*this.level;
			this.levelPoints += 16;
		} else if (tSpin && lines == 2) { //T-Spin Double
			this.score += 1200*this.level;
			this.levelPoints += 16;
		} else if (lines == 4 || (tSpin && lines == 1)) { //tetris or T-Spin Single
			this.score += 800*this.level;
			this.levelPoints += 8;
		} else if (lines == 3) { //triple
			this.score += 500*this.level;
			this.levelPoints += 5;
		} else if (lines == 2) { //double
			this.score += 300*this.level;
			this.levelPoints += 3;
		} else if (lines == 1) { //single
			this.score += 100*this.level;
			this.levelPoints += 1;
		}
		//TODO B2B scoring
		
		updateLevel();
	}
	
	private void updateLevel() {
		//update level based on levelPoints
		while (this.levelPoints > this.level*5) {
			this.levelPoints -= this.level*5;
			this.level++;
		}
		
		this.level = Math.min(this.level, 20); //prevent a higher level than 20
	}
		
	@Override
	public String toString() {
		return "Tetris: " + this.score + " " + this.lines + " " + this.curShape.type;
	}

	public TetrisGame cloneForAI() {
		TetrisGame tg = new TetrisGame(field.getWidth(), field.getHeight(), this.nextShapes.length, this.settings);
		
		tg.field = this.field.cloneForAI();
		
		//clone next/cur/hold shapes
		if (this.curShape != null)
			tg.curShape = this.curShape.clone();
		if (this.holdShape != null)
			tg.holdShape = this.holdShape.clone();
		for (int i = 0; i < this.nextShapes.length; i++)
			tg.nextShapes[i] = this.nextShapes[i].clone();
		
		//clone basic fields
		tg.level = this.level;
		tg.levelPoints = this.levelPoints;
		tg.score = this.score;
		tg.lines = this.lines;
		
		//state fields
		tg.softCount = this.softCount;
		tg.dropTimer = this.dropTimer;
		tg.lockTimer = this.lockTimer;
		tg.pieceHeld = this.pieceHeld;
		tg.lineCombo = this.lineCombo;

		for (Integer i: this.flashRows)
			tg.flashRows.add(i);

		return tg;
	}
	
	enum EventType {
		Line,
		LineCombo,
		TSpin,
		GameOver,
		
		Rotation,
		Movement,
		NotMove,
		StartLockDelay,
		Lock;
	}
	private void triggerEvent(EventType st) {
		triggerEvent(st, -1);
	}
	private void triggerEvent(EventType st, int int1) {
		for (TetrisEventListener ls: this.listeners) {
			switch(st) {
			case Line:
				ls.onNewLine(int1);
				break;
			case LineCombo:
				ls.onLineCombo(int1);
				break;
			case TSpin:
				ls.onTSpin(int1);
				break;
			case GameOver:
				ls.onGameOver();
				break;
			case Rotation:
				ls.onRotation();
				break;
			case Movement:
				ls.onMovement();
				break;
			case NotMove:
				ls.onNotMove();
				break;
			case StartLockDelay:
				ls.onStartLockDelay();
				break;
			case Lock:
				ls.onLock();
				break;
			default:
				break;
			}
		}
	}
}
