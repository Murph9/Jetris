package logic;

import java.util.List;

public interface Tetris {
	
	public void initialise();
	public void update(float tpf);
	
	public Shape.Type curShapeType();
	public List<Cell> curShapeCells();
	public List<Cell> ghostShapeCells();
	public List<Cell> holdShapeCells();
	public List<Cell> nextShapeCells(int i);
	public Cell getCell(int x, int y);
	
	public int getScore();
	public int getLinesCount();
	public int getLevel();

	public boolean newLine();
	public List<Integer> getLines();
	public void triggerLineEnd();
	
	public void hold();
	public void hardDown();
	public void softDown();
	public void moveSide(boolean left);
	public void rotate(boolean right);
	
	public boolean isGameOver();
	
	
	//Design decision:
	//At least the newLine and 'ended' states should be events (via listeners)
}
