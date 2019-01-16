package logic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import logic.ShapeRotator.KickType;

public class Shape {
	
	public enum Type {
		I,T,O,S,Z,J,L;
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
	
	private static final Map<Type, CellColour> TYPE_COLOUR;
	static {
		TYPE_COLOUR = new HashMap<>();
		TYPE_COLOUR.put(Type.I, new CellColour(0.24706f, 0.66275f, 0.81569f, 1f));
		TYPE_COLOUR.put(Type.O, new CellColour(0.94902f, 0.77255f, 0.13333f, 1f));
		TYPE_COLOUR.put(Type.T, new CellColour(0.79216f, 0.43137f, 0.72549f, 1f)); //"purple"
		TYPE_COLOUR.put(Type.S, new CellColour(0.40000f, 0.59608f, 0.24706f, 1f));
		TYPE_COLOUR.put(Type.Z, new CellColour(0.69804f, 0.30588f, 0.27059f, 1f));
		TYPE_COLOUR.put(Type.J, new CellColour(0.00392f, 0.31373f, 0.71765f, 1f));
		TYPE_COLOUR.put(Type.L, new CellColour(0.83137f, 0.44314f, 0.07451f, 1f));
	}
	
	public static Shape getNew(Type t) {
		//info from: http://tetris.wikia.com/wiki/SRS
		switch (t) {//I, O, T, S, Z, J, and L
			case I:
				Shape shape = new Shape(t, KickType.OTHER);
				shape.addCell(new Cell(3,0)); //    
				shape.addCell(new Cell(4,0));
				shape.addCell(new Cell(5,0));
				shape.addCell(new Cell(6,0));
				shape.setCentre(4.5f, 0.5f);
				return shape;
			case O:
				shape = new Shape(t, KickType.OTHER); //doesn't matter what kick type and I looked lonely
				shape.addCell(new Cell(4,0)); //  
				shape.addCell(new Cell(5,0)); //  
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4.5f, 0.5f); //technically nothing
				return shape;
			case T:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,1)); //   
				shape.addCell(new Cell(4,0)); //   
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 1);
				return shape;
			case S:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,1)); //    
				shape.addCell(new Cell(4,0)); //  
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,0));
				shape.setCentre(4, 1);
				return shape;
			case Z:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(5,1)); //  
				shape.addCell(new Cell(4,0)); //    
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(3,0));
				shape.setCentre(4, 1);
				return shape;
			case J:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,0)); // 
				shape.addCell(new Cell(3,1)); //   
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 1);
				return shape;
			case L:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,1)); //     
				shape.addCell(new Cell(4,1)); //   
				shape.addCell(new Cell(5,0));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 1);
				return shape;
			default:
				//TODO log pls
				return null;
		}
	}
	
	////////////////////////////
	protected final LinkedList<Cell> shapeCells; //cells in this shape
	protected final CellColour colour; //colour of this shape
	protected final Type type;
	protected KickType kickType;

	protected Rotation rotState;
	protected float xCentre;
	protected float yCentre; //pixel to rotate about
	
	private Shape(Type t, KickType kickType, CellColour colour) {
		this.type = t;
		this.kickType = kickType;
		this.colour = colour;
		
		this.rotState = Rotation.NONE;
		this.shapeCells = new LinkedList<Cell>();
	}
	private Shape(Type t, KickType kickType) {
		this(t, kickType, TYPE_COLOUR.get(t));
//		TODO setting for random colors H.randomColourHSV()
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

		for (Cell c: shapeCells) {
			float dx = this.xCentre - c.getX();
			float dy = this.yCentre - c.getY();
			
			if (right) {
				c.translate((int)(dx+dy), (int)(dy-dx));
			} else {
				c.translate((int)(dx-dy), (int)(dy+dx));
			}
		}
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
	public CellColour getColour() { return this.colour; }
	
	@Override
	public String toString() { return "Colour: ["+colour+"] Cells" + shapeCells+"]"; }
}