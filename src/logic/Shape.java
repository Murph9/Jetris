package logic;
import java.util.LinkedList;
import java.util.Random;

import com.jme3.math.ColorRGBA;

import logic.ShapeRotator.KickType;


public class Shape {
	
	public enum Type {
		I,T,O,S,Z,J,L,NONE;
	}
	public enum Rotation {
		NONE(0), RIGHT(1), DOUBLE(2), LEFT(3);
		int i;
		Rotation(int i) {
			this.i = i;
		}
		
		public Rotation add(int i) {
			int newInt = (this.i + i + 4) % 4;
			switch(newInt) {
	        case 0:
	            return NONE;
	        case 1:
	            return RIGHT;
	        case 2:
	            return DOUBLE;
	        case 3:
	            return LEFT;
	        }
	        return null;
		}
	}
	
	private static Random rand = new Random();
	public static Shape GenerateRand(ColorRGBA colour) {
		//info from: http://tetris.wikia.com/wiki/SRS
		
		Shape shape = null;
		int number = rand.nextInt(7); //TODO 'bag' random
		switch (number) {//I, O, T, S, Z, J, and L
			case 0: //I
				shape = new Shape(Type.I, KickType.OTHER, colour);
				shape.addCell(new Cell(3,0));
				shape.addCell(new Cell(4,0)); //    
				shape.addCell(new Cell(5,0));
				shape.addCell(new Cell(6,0));
				shape.setCentre(4.5f, 0.5f); //TODO 4.5,0.5
				break;
			case 1: //O
				shape = new Shape(Type.O, KickType.OTHER, colour); //doesn't matter what kick type and I looked lonely
				shape.addCell(new Cell(4,0)); //  
				shape.addCell(new Cell(5,0)); //  
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4.5f, 0.5f); //TODO technically nothing, and technically 4.5, 0.5
				break;
			case 2: //T
				shape = new Shape(Type.T, KickType.NORMAL, colour);
				shape.addCell(new Cell(3,1)); //   
				shape.addCell(new Cell(4,0)); //   
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 1);
				break;
			case 3: //S
				shape = new Shape(Type.S, KickType.NORMAL, colour);
				shape.addCell(new Cell(3,1)); //    
				shape.addCell(new Cell(4,0)); //  
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,0));
				shape.setCentre(4, 1);
				break;
			case 4: //Z
				shape = new Shape(Type.Z, KickType.NORMAL, colour);
				shape.addCell(new Cell(5,1)); //  
				shape.addCell(new Cell(4,0)); //    
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(3,0));
				shape.setCentre(4, 1);
				break;
			case 5: //J
				shape = new Shape(Type.J, KickType.NORMAL, colour);
				shape.addCell(new Cell(3,0)); // 
				shape.addCell(new Cell(3,1)); //   
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 1);
				break;
			case 6: //L
				shape = new Shape(Type.L, KickType.NORMAL, colour);
				shape.addCell(new Cell(3,1)); //     
				shape.addCell(new Cell(4,1)); //   
				shape.addCell(new Cell(5,0));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 1);
				break;
		}
		return shape;
	}
	
	////////////////////////////
	protected final LinkedList<Cell> shapeCells; //cells in this shape
	protected final ColorRGBA colour; //colour of this shape
	protected final Type type;
	protected KickType kickType;

	protected Rotation rotState;
	protected float xCentre;
	protected float yCentre; //pixel to rotate about
	
	private Shape(Type t, KickType kickType, ColorRGBA colour) {
		this.type = t;
		this.kickType = kickType;
		this.colour = colour;
		
		this.rotState = Rotation.NONE;
		this.shapeCells = new LinkedList<Cell>();
	}
	
	public Shape clone() {
		Shape newShape = new Shape(this.type, this.kickType, this.colour);
		for (Cell c: this.getCells()) {
			newShape.addCell(c.clone());
		}
		newShape.xCentre = this.xCentre;
		newShape.yCentre = this.yCentre;
		newShape.rotState = this.rotState;
		return newShape;
	}
	
	protected void rotate(boolean right) {
		this.rotState = this.rotState.add(right ? 1 : -1);

//		if (this.kickType == KickType.OTHER) {
			for (Cell c: shapeCells) {
				float dx = this.xCentre - c.getX();
				float dy = this.yCentre - c.getY();
				
				if (right) {
					c.translate((int)(dx+dy), (int)(dy-dx));
				} else {
					c.translate((int)(dx-dy), (int)(dy+dx));
				}
			}
		/*} else {
			for (Cell c: shapeCells) {
				int dx = (int)this.xCentre - c.getX();
				int dy = (int)this.yCentre - c.getY();
				
				c.translate((int)dx, (int)dy);
				if (right) {
					dx = -dx;
				} else {
					dy = -dy;
				}
				c.translate((int)dy, (int)dx);
			}
		}*/
	}
	
	
	public void addCell(Cell cell) {
		if (shapeCells.size() > 4) { System.out.println("Too Many"); }
		cell.fill(colour);
		shapeCells.add(cell);
	}
	
	//these must be called after init of this
	private void setCentre(float x, float y) {
		this.yCentre = y;
		this.xCentre = x;
	}
	
	public void translate(int x, int y) {
		for (Cell cell: shapeCells) {
			cell.translate(x, y);
		}
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