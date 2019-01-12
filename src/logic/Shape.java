package logic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

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
	
	private static final Type TYPES[] = Type.values();
	private static final Map<Type, CellColour> TYPE_COLOUR;
	static {
		TYPE_COLOUR = new HashMap<>();
		TYPE_COLOUR.put(Type.I, new CellColour(0f, 0.8f, 0.8f, 1f));
		TYPE_COLOUR.put(Type.O, new CellColour(0.8f, 0.8f, 0f, 1f));
		TYPE_COLOUR.put(Type.T, new CellColour(0.8f, 0f, 0.8f, 1f));
		TYPE_COLOUR.put(Type.S, new CellColour(0f, 0.8f, 0f, 1f));
		TYPE_COLOUR.put(Type.Z, new CellColour(0.8f, 0f, 0f, 1f));
		TYPE_COLOUR.put(Type.J, new CellColour(0f, 0f, 0.8f, 1f));
		TYPE_COLOUR.put(Type.L, new CellColour(0.8f*251f / 255f, 0.8f*130f / 255f, 0f, 1f));
	}
	
	private static Random rand = new Random();
	public static Shape GenerateRand() {
		//info from: http://tetris.wikia.com/wiki/SRS
		Shape shape = null;
		Type t = TYPES[rand.nextInt(7)]; //TODO 'bag' random
		switch (t) {//I, O, T, S, Z, J, and L
			case I:
				shape = new Shape(t, KickType.OTHER);
				shape.addCell(new Cell(3,0)); //    
				shape.addCell(new Cell(4,0));
				shape.addCell(new Cell(5,0));
				shape.addCell(new Cell(6,0));
				shape.setCentre(4.5f, 0.5f);
				break;
			case O:
				shape = new Shape(t, KickType.OTHER); //doesn't matter what kick type and I looked lonely
				shape.addCell(new Cell(4,0)); //  
				shape.addCell(new Cell(5,0)); //  
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4.5f, 0.5f); //technically nothing
				break;
			case T:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,1)); //   
				shape.addCell(new Cell(4,0)); //   
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 1);
				break;
			case S:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,1)); //    
				shape.addCell(new Cell(4,0)); //  
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,0));
				shape.setCentre(4, 1);
				break;
			case Z:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(5,1)); //  
				shape.addCell(new Cell(4,0)); //    
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(3,0));
				shape.setCentre(4, 1);
				break;
			case J:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,0)); // 
				shape.addCell(new Cell(3,1)); //   
				shape.addCell(new Cell(4,1));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 1);
				break;
			case L:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,1)); //     
				shape.addCell(new Cell(4,1)); //   
				shape.addCell(new Cell(5,0));
				shape.addCell(new Cell(5,1));
				shape.setCentre(4, 1);
				break;
			default:
				//TODO log pls
				break;
		}
		return shape;
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