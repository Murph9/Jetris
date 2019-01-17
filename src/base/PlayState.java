package base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;

import logic.Cell;
import logic.CellColour;
import logic.Tetris;
import logic.TetrisGame;

public class PlayState extends BaseAppState {
	//manage the playing state of the game, the grid/preview and displaying it
	//yes its a lot but its not a large game [yet]
	
	public static final int X_SIZE = 10;
	public static final int Y_SIZE = 21;
	public static final int Y_HIDDEN = 1; //stupid tetris spec requires a half-hidden row at the top
	public static final int NEXT_SHAPE_COUNT = 3;
	
	private static ColorRGBA DEFAULT_CELL_COLOR = ColorRGBA.BlackNoAlpha;
	private static Vector3f POS_OFFSCREEN = new Vector3f(-100, -100, 0);
	
	private final Main main;
	
	private Material defaultMat;
	private Material defaultMeshMat;
	
	private Node rootNode;
	private Tetris game;
	
	private BitmapText score;
	private BitmapText lines;
	private BitmapText level;
	
	private HashMap<Cell, Geometry> cellMap;
	private List<HashMap<Cell, Geometry>> nextCellMaps;
	private List<Geometry> ghostGeos;
	
	protected Keys keys;
	
	private float flashTimer;
	private float gameOverTimer;
	
	public PlayState(Main m) {
		this.main = m;
	}
	
	public void initialize(Application app) {
		gameOverTimer = 1.5f; //init here because it can only happen once per game
		
		defaultMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		defaultMat.setColor("Color", DEFAULT_CELL_COLOR);
		defaultMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		defaultMeshMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		defaultMeshMat.setColor("Color", DEFAULT_CELL_COLOR);
		defaultMeshMat.getAdditionalRenderState().setWireframe(true);
		defaultMeshMat.getAdditionalRenderState().setLineWidth(4);
		
		this.game = new TetrisGame(X_SIZE, Y_SIZE, NEXT_SHAPE_COUNT);
		
		this.rootNode = new Node("game node");
		this.rootNode.setQueueBucket(Bucket.Gui);
		((SimpleApplication)app).getRootNode().attachChild(rootNode);
		
		
		int screenHeight = ((SimpleApplication)app).getCamera().getHeight();
		int screenWidth = ((SimpleApplication)app).getCamera().getWidth();

		BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
		int fontSize = font.getCharSet().getRenderedSize();
		this.score = new BitmapText(font, false);
		this.score.setLocalTranslation(screenWidth - 150, screenHeight - fontSize, 0);
		this.score.setText("Score: 0");
		this.score.setColor(ColorRGBA.White);
		this.score.setSize(fontSize);
		this.rootNode.attachChild(this.score);
		
		this.lines = new BitmapText(font, false);
		this.lines.setLocalTranslation(screenWidth - 150, screenHeight - fontSize*2, 0);
		this.lines.setText("Lines: 0");
		this.lines.setColor(ColorRGBA.White);
		this.lines.setSize(fontSize);
		this.rootNode.attachChild(this.lines);
		
		this.level = new BitmapText(font, false);
		this.level.setLocalTranslation(screenWidth - 150, screenHeight - fontSize*3, 0);
		this.level.setText("Speed: 0");
		this.level.setColor(ColorRGBA.White);
		this.level.setSize(fontSize);
		this.rootNode.attachChild(this.level);
		
		//calc play field width:
		float cellSize = CellHelper.cellSize(screenHeight, Y_SIZE-Y_HIDDEN);
		float cellMargin = CellHelper.cellMargin(screenHeight, Y_SIZE-Y_HIDDEN);
		float cellSpacing = cellSize + cellMargin;
		
		Mesh b = H.quadCenteredSquare(cellSize/2f);
		
		this.cellMap = new HashMap<>();
		doForEachCell((c) -> {
			Geometry g = initQuad(app.getAssetManager(), DEFAULT_CELL_COLOR, b);
			g.setLocalTranslation(cellPosToView(screenHeight, screenWidth, c.getX(), c.getY(), cellSpacing));
			cellMap.put(c, g);
			rootNode.attachChild(g);
		});
				
		
		this.ghostGeos = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			Geometry g = new Geometry("thing", b);
			g.setMaterial(defaultMeshMat);
			g.setLocalTranslation(POS_OFFSCREEN);
			g.setLocalScale(g.getLocalScale().mult(0.85f)); //so the lines don't overlap
			rootNode.attachChild(g);
			ghostGeos.add(g);
		}
		
		//Init the next piece preview (trying to write this generic so it can be used later when the logic class supports it)
		//pieces always spawn between x[3-6] and y[0,1] so thats how big it is
		nextCellMaps = new ArrayList<HashMap<Cell, Geometry>>(NEXT_SHAPE_COUNT);
		for (int i = 0; i < NEXT_SHAPE_COUNT; i++) {
			Vector3f nextCenter = new Vector3f(screenWidth - cellSpacing*5, screenHeight/2 - 2.5f*i*cellSpacing - cellSpacing, 0); //top/right side hopefully (going down)
			
			HashMap<Cell, Geometry> nextShape = new HashMap<>();
			
			for (int k = 3; k < 7; k++) { //x[3-6]
				for (int j = 0; j < 2; j++) { //y[0-1]
					Cell c = game.getCell(k, j);
					Geometry g = initQuad(app.getAssetManager(), DEFAULT_CELL_COLOR, b);
					nextShape.put(c, g);
					g.setLocalTranslation(nextCenter.add(shapeCellPosToOffset(c.getX(), c.getY(), cellSpacing)));
					rootNode.attachChild(g);
				}
			}
			nextCellMaps.add(nextShape);
		}
		
		
		this.keys = new Keys(this);
		app.getInputManager().addRawInputListener(keys);
		
		this.game.initialise();
	}
	
	private Geometry initQuad(AssetManager am, ColorRGBA c, Mesh b) {
		Geometry g = new Geometry("thing", b);
		Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", c);
		g.setMaterial(mat);
		return g;
	}

	
	public void doAction(String code, boolean pressed, int value) {
		if (!this.isInitialized())
			return;
		if (!pressed)
			return; //only one per key pls

		if (!isEnabled() && code != "Pause")
			return; //only allow pause through on paused
		
		switch(code) {
			case "Pause":
				this.setEnabled(!isEnabled());
				return;
		
			case "HardDrop":
				game.hardDown();
				break;
			case "Drop": //for the this.softCount score (down key usually)
				game.softDown();
				break;
			case "Left":
				game.moveSide(true);
				break;
			case "Right":
				game.moveSide(false);
				break;
			case "RotateLeft":
				game.rotate(false);
				break;
			case "RotateRight":
				game.rotate(true);
				break;
		}
	}
	
	@Override
	public void update(float tpf) {
		super.update(tpf);
		
		if (game.isGameOver()) {
			gameOverTimer -= tpf;
			if (gameOverTimer < 0)
				main.gameLost(new Record(game.getScore(), game.getLinesCount()));
			
			return;
		}
		
		if (!this.isEnabled())
			return; //do not update while paused
		
		game.update(tpf);
		
		this.score.setText("Score: " + game.getScore());
		this.lines.setText("Lines: " + game.getLinesCount());
		this.level.setText("Level: " + game.getLevel());
		
		//update each cell's colour (the expensive way?)
		doForEachCell((c) -> {
			Geometry g = this.cellMap.get(c);
			setColorFromCell(g, c);
		});
		
		//show current piece
		for (Cell c: game.curShapeCells()) {
			Geometry g = this.cellMap.get(c);
			setColorFromCell(g, c);
		}
		//show next pieces
		for (int i = 0 ; i < NEXT_SHAPE_COUNT; i++) {
			for (Geometry g: this.nextCellMaps.get(i).values()) {
				g.setMaterial(defaultMat);
			}
			for (Cell c: game.nextShapeCells(i)) {
				Geometry g = this.nextCellMaps.get(i).get(c);
				setColorFromCell(g, c);
			}
		}
		
		//update the special ghost block geometries
		int geoIndex = 0;
		for (Cell c: game.ghostShapeCells()) {
			Geometry g = this.ghostGeos.get(geoIndex);
			Material mat = defaultMeshMat.clone();
			CellColour col = c.getColour();
			if (col != null)
				mat.setColor("Color", new ColorRGBA(col.r, col.g, col.b, col.a));
			
			g.setMaterial(mat);
			
			Cell c2 = game.getCell(c.getX(), c.getY());
			if (c2 != null) {
				Vector3f pos = new Vector3f(cellMap.get(c2).getLocalTranslation());
				pos.z += 1; //always on top
				g.setLocalTranslation(pos);				
			} else { //outside the play area move out of view
				g.setLocalTranslation(-10, -10, 0);
			}
			
			geoIndex++;
		}
				
		
		//listen to the newline method to start the new line code
		if (game.newLine()) {
			if (flashTimer == 0) {
				//trigger graphics for getting a line
				flashTimer = 0.4f; //units: sec
				
			} else {
				flashTimer -= tpf;
				
				for (Integer j : game.getLines()) {
					for (int i = 0; i < X_SIZE; i++) {
						Cell c = game.getCell(i, j);
						Geometry g = this.cellMap.get(c);
						Material mat = new Material(Main.CURRENT.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
						mat.setColor("Color", ColorRGBA.White);
						g.setMaterial(mat);
					}
				}
				
				if (flashTimer < 0) {
					game.triggerLineEnd();
					flashTimer = 0;
				}
			}

			//remove the ghost block geometries during line highlight phase
			for (Geometry g: this.ghostGeos) {
				g.setLocalTranslation(-100, -100, 0); //off screen
			}
		}
	}
	
	private void setColorFromCell(Geometry g, Cell c) {
		if (g == null || c == null)
			return; //ignore
		
		Material mat = defaultMat.clone();
		CellColour col = c.getColour();
		if (col != null)
			mat.setColor("Color", new ColorRGBA(col.r, col.g, col.b, col.a));
		
		g.setMaterial(mat);
	}

	private void doForEachCell(Consumer<Cell> func) {
		if (func == null)
			throw new NullPointerException("func is null");
		
		for (int i = 0; i < X_SIZE; i++) {
			for (int j = 0; j < Y_SIZE; j++) {
				func.accept(game.getCell(i, j));
			}
		}
	}
	
	//calculates position based off screen center
	private static Vector3f cellPosToView(int screenHeight, int screenWidth, int x, int y, float cellSpacing) {
		//      8     [* = screen center]
		//      9
		//-2-3-4*5-6-7-
		//      10
		//      11
		
		float offX = x - (X_SIZE) / 2f + 0.5f;
		float offY = -y + (Y_SIZE + Y_HIDDEN) / 2f - 0.5f;

		return new Vector3f(
			offX * cellSpacing + screenWidth / 2f,
			offY * cellSpacing + screenHeight / 2f, //TODO offset so you can see the top row a little
			0
		);
	}
	//calculates position based off of 0,0 (in screen scale) and a x y offset of 4.5, 0.5
	private static Vector3f shapeCellPosToOffset(int x, int y, float cellSpacing) {
		//      0    [* = screen center]
		//-2-3-4*5-6-7-
		//      1
	
		float offX = x - (4.5f) / 2f + 0.5f;
		float offY = -y + (0.5f) / 2f + 0.5f;
		return new Vector3f(
			offX * cellSpacing,
			offY * cellSpacing,
			0
		);
	}



	@Override
	protected void cleanup(Application app) {
		((SimpleApplication)app).getRootNode().detachChild(rootNode);
	}
	@Override
	protected void onEnable() {}
	@Override
	protected void onDisable() { }
}
