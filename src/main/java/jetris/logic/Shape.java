package jetris.logic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jetris.base.H;
import jetris.logic.ShapeRotator.KickType;
import jetris.saving.ISettings;
import jetris.saving.SettingsManager;

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
		
		/**Rotate shape by i, +ve i means right (clockwise)*/
		public Rotation add(int i) {
			int newInt = (this.i + i + 4) % 4;
			switch(newInt) {
	        case 0:
	            return NONE;
	        case 1:
	            return RIGHT; //clockwise
	        case 2:
	            return DOUBLE;
	        case 3:
	            return LEFT; //counter-clockwise
	        }
	        return null;
		}
	}
	
	private static final Map<Type, CellColour> TYPE_COLOUR;
	static {
		TYPE_COLOUR = new HashMap<>();
		TYPE_COLOUR.put(Type.I, new CellColour(0.24706f, 0.66275f, 0.81569f, 1f));
		TYPE_COLOUR.put(Type.O, new CellColour(0.94902f, 0.77255f, 0.13333f, 1f));
		TYPE_COLOUR.put(Type.T, new CellColour(0.79206f, 0.43137f, 0.72549f, 1f)); //"purple"
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
				shape.addCell(new Cell(3,20)); //    
				shape.addCell(new Cell(4,20));
				shape.addCell(new Cell(5,20));
				shape.addCell(new Cell(6,20));
				shape.setCentre(4.5f, 19.5f); //bottom middle
				return shape;
			case O:
				shape = new Shape(t, KickType.OTHER); //doesn't matter what kick type and I looked lonely
				shape.addCell(new Cell(4,20)); //  
				shape.addCell(new Cell(5,20)); //  
				shape.addCell(new Cell(4,21));
				shape.addCell(new Cell(5,21));
				shape.setCentre(4.5f, 20.5f); //center of the 4 cells
				return shape;
			case T:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,20)); //   
				shape.addCell(new Cell(4,21)); //   
				shape.addCell(new Cell(4,20));
				shape.addCell(new Cell(5,20));
				shape.setCentre(4, 20); //bottom middle
				return shape;
			case S:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,20)); //    
				shape.addCell(new Cell(4,21)); //  
				shape.addCell(new Cell(4,20));
				shape.addCell(new Cell(5,21));
				shape.setCentre(4, 20);
				return shape;
			case Z:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(5,20)); //  
				shape.addCell(new Cell(4,21)); //    
				shape.addCell(new Cell(4,20));
				shape.addCell(new Cell(3,21));
				shape.setCentre(4, 20);
				return shape;
			case J:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,21)); // 
				shape.addCell(new Cell(3,20)); //   
				shape.addCell(new Cell(4,20));
				shape.addCell(new Cell(5,20));
				shape.setCentre(4, 20);
				return shape;
			case L:
				shape = new Shape(t, KickType.NORMAL);
				shape.addCell(new Cell(3,20)); //     
				shape.addCell(new Cell(4,20)); //   
				shape.addCell(new Cell(5,21));
				shape.addCell(new Cell(5,20));
				shape.setCentre(4, 20);
				return shape;
			default:
				return null;
		}
	}
	
	////////////////////////////
	protected final LinkedList<Cell> shapeCells; //cells in this shape
	protected CellColour colour; //colour of this shape
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
		
		//should be cached to be called like this
		ISettings sets = SettingsManager.load(); //PLS: cross library calls 
		if (sets.randomColours()) //cross library call
			this.colour = H.randomColourHSV();
		
		if (sets.greyScale()) {
			float grey = 0.299f * this.colour.r + 0.587f * this.colour.g + 0.114f * this.colour.b;
			this.colour = new CellColour(grey, grey, grey, 1); //formula from wiki
			//PLS: change to using a shader
		}
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
				c.translate((int)(dx-dy), (int)(dy+dx));
			} else {
				c.translate((int)(dx+dy), (int)(dy-dx));
			}
		}
	}
	
	
	public void addCell(Cell cell) {
		if (shapeCells.size() > 4) { System.out.println("Too many cells"); }
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
	public String toString() { 
		return "rot:" + this.rotState + " center:(" 
			+ this.xCentre + "," + this.yCentre + ") colour: ["+colour+"]";
	}
}