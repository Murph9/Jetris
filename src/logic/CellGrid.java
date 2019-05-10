package logic;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CellGrid {

	private final int width;
	private final int height;
	private final Cell[][] cells;
	
	protected CellGrid(int width, int height) {
		this.width = width;
		this.height = height;
		
		this.cells = new Cell[this.height][this.width];
		
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				this.cells[y][x] = new Cell(x, y);
			}
		}
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public boolean isValidPos(Shape nextState) {
		for (Cell c: nextState.getCells()) {
			if (c.getX() >= width || c.getX() < 0) 
				return false;
			if (c.getY() < 0)
				return false;
			
			if (isCellFilled(c.getX(), c.getY()))
				return false;
		}
		return true;
	}
	
	/** Is the cell filled */
	public boolean isCellFilled(int x, int y) {
		return isCellFilled(x, y, false);
	}
	
	/** Is the cell filled, with a default */
	public boolean isCellFilled(int x, int y, boolean base) {
		if (x < 0 || x >= width) return base;
		if (y < 0 || y >= height) return base;
		return cells[y][x].getFilled();
	}
	
	public void fillCell(int x, int y, CellColour colour) {
		if (x < 0 || y < 0)
			return;
		cells[y][x].fill(colour);
	}
	
	public Cell getCell(int x, int y) {
		if (x < 0 || x >= width) return null;
		if (y < 0 || y >= height) return null;
		
		return cells[y][x];
	}
	
	public void removeRows(List<Integer> rows) {
		Collections.sort(rows, Collections.reverseOrder()); //must start with the top row, and go down
		
		for (Integer i: rows)
			removeRow(i);
	}
	private void removeRow(int row) {
		//assumes the row given is valid (doesn't validate the filled row)
		for (int x = 0; x < this.width; x++) {
			cells[row][x].clear();
		}
		
		//start at the row and move everything down
		for (int y = row+1; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				cells[y-1][x].fill(cells[y][x].getColour());
				cells[y][x].clear();
			}
		}
	}
	
	public int minDrop(Shape shape) {
		if (shape == null)
			return -1;
		
		int length = Integer.MAX_VALUE;
		for (Cell c : shape.getCells()) {
			int temp = 0;
			for (int i = c.getY(); i >= 0; i--) {
				if (isCellFilled(c.getX(), i)) {
					length = Math.min(length, temp); //we want the smallest distance for a drop
					break;
				}
				temp++;
			}
			length = Math.min(length, temp);
		}
		return length;
	}
	
	public List<Integer> getFullRows() {
		List<Integer> rows = new LinkedList<Integer>();
		for (int y = 0; y < this.height; y++) {
			boolean full = true;
			for (int x = 0; x < this.width; x++) {
				if (!cells[y][x].getFilled()) {
					full = false;
					break;
				}
			}
			if (full == true) {
				rows.add(y);
			}
		}
		return rows;
	}
	
	public CellGrid cloneForAI() {
		CellGrid cg = new CellGrid(this.width, this.height);
		//clone cell grid
		for (int i = 0; i < this.cells.length; i++)
			for (int j = 0; j < this.cells[i].length; j++)
				cg.cells[i][j] = this.cells[i][j].clone();
		
		return cg;
	}
}
