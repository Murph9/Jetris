package base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import logic.Cell;
import logic.Tetris;
import logic.Tetris.GameType;
import logic.TetrisGame;

public class PlayState extends BaseAppState {
	//manage the playing state of the game, the grid/preview and displaying it
	//yes its a lot but its not a large game [yet]
	
	private static int X_SIZE = 10;
	private static int Y_SIZE = 21;
	private static int Y_HIDDEN = 1; //stupid tetris spec
	
	private final Main main;
	private final GameType type;
	private final int bCount;
	private final Material defaultMat;
	private final Material defaultMeshMat;
		
	private Node rootNode;
	private Tetris game;
	
	private BitmapText score;
	private BitmapText lines;
	private BitmapText level;
		
	private HashMap<Cell, Geometry> cellMap;
	private HashMap<Cell, Geometry> nextCellMap;
	private List<Geometry> ghostGeos;
	
	private Keys keys;
	
	private float flashTimer;
	
	public PlayState(Main m, GameType type, int bCount) {
		this.main = m;
		this.type = type;
		this.bCount = bCount;
		defaultMat = new Material(Main.CURRENT.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		defaultMeshMat = new Material(Main.CURRENT.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	}
	
	
	public void initialize(Application app) {
		
		defaultMat.setColor("Color", new ColorRGBA(0.95f,0.95f,0.95f,1));
		defaultMeshMat.setColor("Color", new ColorRGBA(0.95f,0.95f,0.95f,1));
		defaultMeshMat.getAdditionalRenderState().setWireframe(true);
		defaultMeshMat.getAdditionalRenderState().setLineWidth(4);
		
		this.game = new TetrisGame(X_SIZE, Y_SIZE);
		
		this.rootNode = new Node("game node");
		this.rootNode.setQueueBucket(Bucket.Gui);
		Main.CURRENT.getRootNode().attachChild(rootNode);
		
		
		int screenHeight = Main.CURRENT.getViewPort().getCamera().getHeight();
		int screenWidth = Main.CURRENT.getViewPort().getCamera().getWidth();

		BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
		int fontSize = font.getCharSet().getRenderedSize();
		this.score = new BitmapText(font, false);
		this.score.setLocalTranslation(screenWidth - 150, screenHeight - fontSize, 0);
		this.score.setText("Score: 0");
		this.score.setColor(ColorRGBA.Black);
		this.score.setSize(fontSize);
		this.rootNode.attachChild(this.score);
		
		this.lines = new BitmapText(font, false);
		this.lines.setLocalTranslation(screenWidth - 150, screenHeight - fontSize*2, 0);
		this.lines.setText("Lines: 0");
		this.lines.setColor(ColorRGBA.Black);
		this.lines.setSize(fontSize);
		this.rootNode.attachChild(this.lines);
		
		this.level = new BitmapText(font, false);
		this.level.setLocalTranslation(screenWidth - 150, screenHeight - fontSize*3, 0);
		this.level.setText("Speed: 0");
		this.level.setColor(ColorRGBA.Black);
		this.level.setSize(fontSize);
		this.rootNode.attachChild(this.level);
		
		
		int margin = (Y_SIZE-Y_HIDDEN+1) * 2;
		float boxSize = (screenHeight - margin)/(Y_SIZE-Y_HIDDEN) * 0.5f - 0.5f; //0.5 = box is twice as big (-0.5 for pixel offsets)
		Box b = new Box(boxSize, boxSize, boxSize);
		
		this.cellMap = new HashMap<>();
		doForEachCell((c) -> {
			ColorRGBA col = ColorRGBA.DarkGray; //nothing
			Geometry g = initCellBox(app.getAssetManager(), b, col);
			cellMap.put(c, g);
			g.setLocalTranslation(cellPosToView(screenHeight, screenWidth, boxSize*2, 2, c.getX(), c.getY()));
			rootNode.attachChild(g);
		});
		
		this.nextCellMap = new HashMap<>();
		for (int i = 3; i < 7; i++) {
			for (int j = 0; j < 3; j++) {
				Cell c = game.getCell(i, j);
				ColorRGBA col = ColorRGBA.DarkGray; //nothing
				Geometry g = initCellBox(app.getAssetManager(), b, col);
				nextCellMap.put(c, g);
				g.setLocalTranslation(nextCellPosToView(screenHeight, screenWidth, boxSize*2, 2, c.getX(), c.getY()));
				rootNode.attachChild(g);
			}
		}
		
		
		this.ghostGeos = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			Geometry g = new Geometry("thing", b);
			g.setMaterial(defaultMeshMat);
			g.setLocalTranslation(-10, -10, -10000);
			g.setLocalScale(g.getLocalScale().mult(0.85f)); //so the lines don't overlap
			rootNode.attachChild(g);
			ghostGeos.add(g);
		}
		
		this.keys = new Keys(this);
		app.getInputManager().addRawInputListener(keys);
		
		this.game.initialise(this.type, this.bCount);
	}
	
	private Geometry initCellBox(AssetManager am, Box b, ColorRGBA c) {
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
				game.rotate(true);
				break;
			case "RotateRight":
				game.rotate(false);
				break;
		}
	}
	
	@Override
	public void update(float tpf) {
		super.update(tpf);
		
		if (game.isGameOver()) {
			//finish 
			//TODO maybe something slower?
			main.gameLost(type, this.bCount, new Record(game.getScore(), game.getLinesCount()));
			
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
		//show next piece
		for (Geometry g: this.nextCellMap.values()) {
			g.setMaterial(defaultMat);
		}
		for (Cell c: game.nextShapeCells()) {
			Geometry g = this.nextCellMap.get(c);
			setColorFromCell(g, c);
		}
		
		//update the special ghost block geometries
		int geoIndex = 0;
		for (Cell c: game.ghostShapeCells()) {
			Geometry g = this.ghostGeos.get(geoIndex);
			Material mat = defaultMeshMat.clone();
			ColorRGBA col = c.getColour();
			if (col != null)
				mat.setColor("Color", col);
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
		ColorRGBA col = c.getColour();
		if (col != null)
			mat.setColor("Color", col);
		
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
	
	private static Vector3f cellPosToView(int height, int width, float boxSize, int margin, int x, int y) {
		Vector3f a = new Vector3f(0, 0, -1);
		a.x = ((x - (X_SIZE)/2 + 0.5f)*(boxSize+margin)) + width/2f; //offset from the center
		a.y = ((-y + (Y_SIZE+Y_HIDDEN)/2 - 0.5f)*(boxSize+margin)) + height/2f;
		return a;
	}
	private static Vector3f nextCellPosToView(int height, int width, float boxSize, int margin, int x, int y) {
		float fy = (-y + Y_SIZE - 0.5f - Y_HIDDEN)/(float)(Y_SIZE-Y_HIDDEN);
		
		Vector3f a = new Vector3f(x - 3 + 0.5f, fy, -1);
		a.x *= boxSize+margin;
		a.y *= height;
		return a;
	}



	@Override
	protected void cleanup(Application app) {
		Main.CURRENT.getRootNode().detachChild(rootNode);
	}
	@Override
	protected void onEnable() {}
	@Override
	protected void onDisable() { }
}
