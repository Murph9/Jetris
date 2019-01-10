package logic;

import java.util.List;

public interface Tetris {

	public enum GameType {
		TYPE_A, TYPE_B, NONE;
	}
	
	public void initialise(GameType type, int lines);
	public void update(float tpf);
	
	public List<Cell> curShapeCells();
	public List<Cell> ghostShapeCells();
	public List<Cell> nextShapeCells();
	public Cell getCell(int x, int y);
	
	public int getScore();
	public int getLinesCount();
	public int getLevel();

	public boolean newLine();
	public List<Integer> getLines();
	public void triggerLineEnd();
	
	public void hardDown();
	public void softDown();
	public void moveSide(boolean left);
	public void rotate(boolean left);
	
	public boolean isGameOver();
	
	
	//Design decision:
	//At least the newLine and 'ended' states should be events (via listeners)
}
