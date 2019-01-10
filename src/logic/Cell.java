package logic;

import com.jme3.math.ColorRGBA;


public class Cell {
	
	private int x;
	private int y;
	private ColorRGBA colour;
	
	private boolean full;
	
	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
		this.colour = null;
		this.full = false;
	}
	
	public void translate(int dx, int dy) {
		this.x += dx;
		this.y += dy;
	}
	
	//makes it 'there'
	public void fill(ColorRGBA colour) {
		this.colour = colour;
		this.full = true;
	}
	
	//make it 'not there'
	public void clear() {
		this.colour = null;
		this.full = false;
	}
	
	public int getX() {	return this.x; }
	public int getY() { return this.y; }
	public ColorRGBA getColour() { return this.colour; }
	public boolean getFilled() { return this.full; }
	
	
	public Cell clone() {
		Cell cloned = new Cell(this.x, this.y);
		cloned.fill(this.colour);
		return cloned;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Cell))
			return false;
		Cell c = (Cell)obj;
		return c.x == x && c.y == y;
	}
	@Override
	public int hashCode() {
		return x*2903 + 1907*y;
	}
}