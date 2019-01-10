package logic;
import java.util.LinkedList;
import java.util.Random;

import com.jme3.math.ColorRGBA;


public class Shape {
	
	public enum Type {
		I,T,O,S,Z,J,L,NONE;
	}
	private enum Rotation {
		NONE, NORMAL, WEIRD;
	}
	
	private static Random rand = new Random();
	public static Shape GenerateRand(ColorRGBA colour) {
		//TODO http://tetris.wikia.com/wiki/SRS
		
		Shape shape = null;
		int number = rand.nextInt(7);
		switch (number) {//I, O, T, S, Z, J, and L
			case 0: //I
				shape = new Shape(Type.I, Rotation.WEIRD, colour);
				shape.addCell(new Cell(3,0));
				shape.addCell(new Cell(4,0)); //    
				shape.addCell(new Cell(5,0));
				shape.addCell(new Cell(6,0));
				shape.setCentre(5, 0);
				break;
			case 1: //O
				shape = new Shape(Type.O, Rotation.NONE, colour);
				shape.addCell(new Cell(4,0)); //  
				shape.addCell(new Cell(5,0)); //  
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 0); //just in case
				break;
			case 2: //T
				shape = new Shape(Type.T, Rotation.NORMAL, colour);
				shape.addCell(new Cell(3,0)); //   
				shape.addCell(new Cell(4,0)); //   
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,0));
				shape.setCentre(4, 0);
				break;
			case 3: //S
				shape = new Shape(Type.S, Rotation.WEIRD, colour);
				shape.addCell(new Cell(4,0)); //    
				shape.addCell(new Cell(4,1)); //  
				shape.addCell(new Cell(5,0));
				shape.addCell(new Cell(3,1));
				shape.setCentre(4, 0);
				break;
			case 4: //Z
				shape = new Shape(Type.Z, Rotation.WEIRD, colour);
				shape.addCell(new Cell(5,1)); //  
				shape.addCell(new Cell(4,0)); //    
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(3,0));
				shape.setCentre(4, 0);
				break;
			case 5: //J
				shape = new Shape(Type.J, Rotation.NORMAL, colour);
				shape.addCell(new Cell(3,0)); //   
				shape.addCell(new Cell(4,0)); //     
				shape.addCell(new Cell(5,0));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 0);
				break;
			case 6: //L
				shape = new Shape(Type.L, Rotation.NORMAL, colour);
				shape.addCell(new Cell(3,1)); //   
				shape.addCell(new Cell(3,0)); // 
				shape.addCell(new Cell(4,0));
				shape.addCell(new Cell(5,0));
				shape.setCentre(4, 0);
				break;
		}
		return shape;
	}
	
	////////////////////////////
	protected final LinkedList<Cell> shapeCells; //cells in this shape
	protected final ColorRGBA colour; //colour of this shape
	protected final Type type;
	protected final Rotation rotType;
	
	protected int xCentre;
	protected int yCentre; //pixel to rotate about
	
	//weird rotation type:
	private boolean rotateState; //either rotate left, or right... (but only once)
	
	private Shape(Type t, Rotation rotType, ColorRGBA colour) {
		this.type = t;
		this.rotType = rotType;
		this.colour = colour;
		this.shapeCells = new LinkedList<Cell>();
	}
	
	public Shape clone() {
		Shape newShape = new Shape(this.type, this.rotType, this.colour);
		for (Cell c: this.getCells()) {
			newShape.addCell(c.clone());
		}
		newShape.xCentre = this.xCentre;
		newShape.yCentre = this.yCentre;
		newShape.rotateState = this.rotateState;
		return newShape;
	}
	
	//NOTE THE LACK OF WALL PUSHING IN abstract methods
	public void rotate(int direction, int xMax, int yMax) {
		
		if (rotType == Rotation.NORMAL) {
			int tempX = 0;
			int tempY = 0;
			
			for (Cell c: shapeCells) {
				tempY = c.getX() - this.xCentre;
				tempX = c.getY() - this.yCentre;
				c.translate(-tempY, -tempX);
				if (direction == 0) {
					tempX = -tempX; //right
				} else {
					tempY = -tempY; //left?
				}
				c.translate(-tempX, -tempY);
			}
		} else if (rotType == Rotation.WEIRD) {
			//note direction is irrelevant for the ShapeWeird objects
			int tempX = 0;
			int tempY = 0;
			
			for (Cell c: shapeCells) {
				tempY = c.getX() - this.xCentre;
				tempX = c.getY() - this.yCentre;
				c.translate(-tempY, -tempX);
				if (this.rotateState) { 
					tempX = -tempX;
				} else {
					tempY = -tempY;
				}
				c.translate(-tempX, -tempY);
			}
			
			this.rotateState = !rotateState;
		}
	}
	
	
	public void addCell(Cell cell) {
		if (shapeCells.size() > 4) { System.out.println("Too Many"); }
		cell.fill(colour);
		shapeCells.add(cell);
	}
	
	//these must be called after init of this
	private void setCentre(int x, int y) {
		this.yCentre = y;
		this.xCentre = x;
	}
	
	public void translate(int x, int y) {
		for (Cell cell: shapeCells) {
			cell.translate(x, y);
		}
		if (xCentre==-1 && yCentre==-1) return;
		xCentre += x; //yeah don't forget these guys
		yCentre += y;
	}
	
	public LinkedList<Cell> getCells() {
		return new LinkedList<Cell>(this.shapeCells);
	}
	public ColorRGBA getColour() { return this.colour; }
	
	@Override
	public String toString() { return "Colour: ["+colour+"] Cells" + shapeCells+"]"; }
}