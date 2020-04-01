package jetris.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

import jetris.logic.Cell;
import jetris.logic.CellColour;
import jetris.logic.LogicSettings;
import jetris.logic.Tetris;
import jetris.logic.TetrisGame;
import jetris.saving.ISettings;
import jetris.saving.Record;
import jetris.saving.SettingsManager;

public class PlayState extends BaseAppState {
	//manage the playing state of the game, the grid/preview and displaying it
	//yes its a lot but its not a large game [yet]
	
	public static final int X_SIZE = 10;
	public static final int Y_SIZE = 25; //by the tetris spec its meant to be 40
	public static final int Y_HIDDEN = 5; //we want 20 on the field
	public static final int NEXT_SHAPE_COUNT = 3;
	
	//http://tetris.wikia.com/wiki/Line_clear#Delay
	protected static final float LINE_DELAY = 0.5f; //units: sec TODO should be logic side
	
	private static ColorRGBA DEFAULT_CELL_COLOR = ColorRGBA.BlackNoAlpha;
	private static Vector3f POS_OFFSCREEN = new Vector3f(-100, -100, 0);
	
	private final Main main;
	
	private Material defaultMat;
	private Material defaultMeshMat;
	
	private Node rootNode;
	protected Tetris engine;
	
	private BitmapText scoreText;
	private BitmapText linesText;
	private BitmapText levelText;
	private BitmapText pausedText;
	
	private HashMap<Cell, Geometry> cellMap;
	private List<Geometry> ghostGeos;
	
	private HashMap<Cell, Geometry> holdCellMap;
	private List<HashMap<Cell, Geometry>> nextCellMaps;
	
	protected Keys keys;
	
	private float flashTimer;
	private float gameOverTimer;
	
	private boolean paused;
	private float pauseTimer;
	
	private PlayStateSoundManager sounds;
	
	private static final int EVENT_LOG_SIZE = 10;
	private LinkedList<String> logEntries;
	private PlayStateEventLogger logger;
	private BitmapText eventLog;
	
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
		
		ISettings settings = SettingsManager.load();
		
		LogicSettings gameSettings = new LogicSettings();
		gameSettings.hardDropLock = settings.hardDropLock();
		gameSettings.invisibleLockedCells = settings.expertMode();
		gameSettings.useHoldPiece = settings.useHoldPiece();
		this.engine = new TetrisGame(X_SIZE, Y_SIZE, NEXT_SHAPE_COUNT, gameSettings);
		
		this.rootNode = new Node("game node");
		this.rootNode.setQueueBucket(Bucket.Gui);
		((SimpleApplication)app).getRootNode().attachChild(rootNode);
		
		//init sounds
		if (settings.useSoundEffects()) {
			this.sounds = new PlayStateSoundManager(this.engine);
			app.getStateManager().attach(sounds);
		}
		
		//init visual things
		int screenHeight = ((SimpleApplication)app).getCamera().getHeight();
		int screenWidth = ((SimpleApplication)app).getCamera().getWidth();

		BitmapFont font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
		int fontSize = font.getCharSet().getRenderedSize();
		this.scoreText = new BitmapText(font, false);
		this.scoreText.setLocalTranslation(screenWidth - 150, screenHeight - fontSize, 0);
		this.scoreText.setText("Score: 0");
		this.scoreText.setColor(ColorRGBA.White);
		this.scoreText.setSize(fontSize);
		this.rootNode.attachChild(this.scoreText);
		
		this.linesText = new BitmapText(font, false);
		this.linesText.setLocalTranslation(screenWidth - 150, screenHeight - fontSize*2, 0);
		this.linesText.setText("Lines: 0");
		this.linesText.setColor(ColorRGBA.White);
		this.linesText.setSize(fontSize);
		this.rootNode.attachChild(this.linesText);
		
		this.levelText = new BitmapText(font, false);
		this.levelText.setLocalTranslation(screenWidth - 150, screenHeight - fontSize*3, 0);
		this.levelText.setText("Speed: 0");
		this.levelText.setColor(ColorRGBA.White);
		this.levelText.setSize(fontSize);
		this.rootNode.attachChild(this.levelText);
		
		
		this.eventLog = new BitmapText(font, false);
		this.eventLog.setLocalTranslation(50, screenHeight - fontSize*1, 0);
		this.eventLog.setText("...");
		this.eventLog.setColor(ColorRGBA.White);
		this.eventLog.setSize(fontSize);
		this.rootNode.attachChild(this.eventLog);

		this.logger = new PlayStateEventLogger((String log) -> {
			addEventLogEntry(log);
		});
		this.engine.addEventListener(logger);
		this.logEntries = new LinkedList<String>();
		
		this.pausedText = new BitmapText(font, false);
		this.pausedText.setLocalTranslation(screenWidth/2, screenHeight/2, 0);
		this.pausedText.setText("Paused");
		this.pausedText.setColor(ColorRGBA.White);
		this.pausedText.setSize(fontSize);
		
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
		//pieces always spawn between x[3-6] and y[20,21] so thats how big it is
		nextCellMaps = new ArrayList<HashMap<Cell, Geometry>>(NEXT_SHAPE_COUNT);
		for (int i = 0; i < NEXT_SHAPE_COUNT; i++) {
			Vector3f nextCenter = new Vector3f(screenWidth - cellSpacing*5, screenHeight/2 - 2.5f*i*cellSpacing - cellSpacing, 0); //top/right side hopefully (going down)
			
			HashMap<Cell, Geometry> nextShape = new HashMap<>();
			
			for (int k = 3; k < 7; k++) { //x[3-6]
				for (int j = 20; j < 22; j++) { //y[20-21]
					Cell c = engine.getCell(k, j);
					Geometry g = initQuad(app.getAssetManager(), DEFAULT_CELL_COLOR, b);
					nextShape.put(c, g);
					g.setLocalTranslation(nextCenter.add(shapeCellPosToOffset(c.getX(), c.getY(), cellSpacing)));
					rootNode.attachChild(g);
				}
			}
			nextCellMaps.add(nextShape);
		}
		//then the hold shape bottom left
		Vector3f center = new Vector3f(0, 2.5f*cellSpacing, 0); //top/right side hopefully (going up)
		this.holdCellMap = new HashMap<>();
		for (int k = 3; k < 7; k++) { //x[3-6]
			for (int j = 20; j < 22; j++) { //y[20-21]
				Cell c = engine.getCell(k, j);
				Geometry g = initQuad(app.getAssetManager(), DEFAULT_CELL_COLOR, b);
				holdCellMap.put(c, g);
				g.setLocalTranslation(center.add(shapeCellPosToOffset(c.getX(), c.getY(), cellSpacing)));
				rootNode.attachChild(g);
			}
		}		
		
		this.keys = new Keys(this);
		app.getInputManager().addRawInputListener(keys);
		
		this.engine.initialise();
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
			
			case "Hold":
				engine.hold();
				break;

			case "HardDrop":
				engine.hardDown();
				break;
			case "Drop": //for the this.softCount score (down key usually)
				engine.softDown();
				break;
			case "Left":
				engine.moveSide(true);
				break;
			case "Right":
				engine.moveSide(false);
				break;
			case "RotateLeft":
				engine.rotate(false);
				break;
			case "RotateRight":
				engine.rotate(true);
				break;
		}
	}
	
	@Override
	public void update(float tpf) {
		super.update(tpf);
		
		if (engine.isGameOver()) {
			gameOverTimer -= tpf;
			if (gameOverTimer < 0)
				main.gameLost(this, new Record(engine.getScore(), engine.getLinesCount()));
			
			return;
		}
		
		//unpausing causes a pauseTimer to be triggered
		if (!paused && pauseTimer > 0) {
			pauseTimer -= tpf;
			if (pauseTimer < 0) {
				this.pausedText.setText("Paused");
				this.rootNode.detachChild(this.pausedText);
				pauseTimer = 0;
			} else if (pauseTimer < 1) {
				this.pausedText.setText("1");
			} else if (pauseTimer < 2) {
				this.pausedText.setText("2");
			} else if (pauseTimer < 3) {
				this.pausedText.setText("3");
			}
			return;
		}
		
		//update game engine, will never be called while this isn't enabled
		engine.update(tpf);
				

		renderView();
		
		
		//listen to the newline method to start the new line code
		if (engine.newLine()) {
			if (flashTimer == 0) {
				//trigger graphics for getting a line
				flashTimer = LINE_DELAY;
				
			} else {
				flashTimer -= tpf;
				
				for (Integer j : engine.getLines()) {
					for (int i = 0; i < X_SIZE; i++) {
						Cell c = engine.getCell(i, j);
						Geometry g = this.cellMap.get(c);
						Material mat = new Material(Main.CURRENT.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
						mat.setColor("Color", ColorRGBA.White);
						g.setMaterial(mat);
					}
				}
				
				if (flashTimer < 0) {
					engine.triggerLineEnd();
					flashTimer = 0;
				}
			}

			//remove the ghost block geometries during line highlight phase
			for (Geometry g: this.ghostGeos) {
				g.setLocalTranslation(POS_OFFSCREEN);
			}
		}
	}
	
	private void renderView() {
		//update the state of all of the visuals
		
		this.scoreText.setText("Score: " + engine.getScore());
		this.linesText.setText("Lines: " + engine.getLinesCount());
		this.levelText.setText("Level: " + engine.getLevel());
		
		//update each cell's colour (the expensive way?)
		doForEachCell((c) -> {
			Geometry g = this.cellMap.get(c);
			setColorFromCell(g, c);
		});

		//show current piece
		for (Cell c: engine.curShapeCells()) {
			Geometry g = this.cellMap.get(c);
			setColorFromCell(g, c);
		}
		//show next pieces
		for (int i = 0 ; i < NEXT_SHAPE_COUNT; i++) {
			for (Geometry g: this.nextCellMaps.get(i).values()) {
				g.setMaterial(defaultMat);
			}
			for (Cell c: engine.nextShapeCells(i)) {
				Geometry g = this.nextCellMaps.get(i).get(c);
				setColorFromCell(g, c);
			}
		}
		//show hold piece
		for (Geometry g: this.holdCellMap.values()) {
			g.setMaterial(defaultMat);
		}
		for (Cell c: engine.holdShapeCells()) {
			Geometry g = this.holdCellMap.get(c);
			setColorFromCell(g, c);
		}
		
		
		//yes it looks like this updates in the render method, but i can assure you its only visual 
		if (SettingsManager.load().ghost()) {
			//update the special ghost block geometries
			int geoIndex = 0;
			for (Cell c: engine.ghostShapeCells()) {
				Geometry g = this.ghostGeos.get(geoIndex);
				Material mat = defaultMeshMat.clone();
				CellColour col = c.getColour();
				if (col != null)
					mat.setColor("Color", new ColorRGBA(col.r, col.g, col.b, col.a));
				if (this.paused) //paused, so remove
					mat.setColor("Color", DEFAULT_CELL_COLOR);
				g.setMaterial(mat);
				
				Cell c2 = engine.getCell(c.getX(), c.getY());
				if (c2 != null) {
					Vector3f pos = new Vector3f(cellMap.get(c2).getLocalTranslation());
					pos.z += 1; //always on top
					g.setLocalTranslation(pos);
				} else { //outside the play area move out of view
					g.setLocalTranslation(-10, -10, 0);
				}
				
				geoIndex++;
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
		if (this.paused) //paused, so remove
			mat.setColor("Color", DEFAULT_CELL_COLOR);
		
		g.setMaterial(mat);
	}

	private void doForEachCell(Consumer<Cell> func) {
		if (func == null)
			throw new NullPointerException("func is null");
		
		for (int i = 0; i < X_SIZE; i++) {
			for (int j = 0; j < Y_SIZE; j++) {
				func.accept(engine.getCell(i, j));
			}
		}
	}
	
	//calculates position based off screen center
	private static Vector3f cellPosToView(int screenHeight, int screenWidth, int x, int y, float cellSpacing) {
		//      11     [** = screen center]
		//      10
		//-2-3-4**5-6-7-
		//       9
		//       8
		
		float offX = x - (X_SIZE) / 2f + 0.5f;
		float offY = y - (Y_SIZE - Y_HIDDEN) / 2f + 0.5f;

		return new Vector3f(
			offX * cellSpacing + screenWidth / 2f,
			offY * cellSpacing + screenHeight / 2f - cellSpacing/15f, //an offset so you can see the top row a little (required by guidelines)
			0
		);
	}
	//calculates position based off of 0,0 (in screen scale) and a x y offset of 4.5, 0.5
	private static Vector3f shapeCellPosToOffset(int x, int y, float cellSpacing) {
		//      21    [** = screen center]
		//-2-3-4**5-6-7-
		//      20
	
		float offX = x - (4.5f) / 2f + 0.5f;
		float offY = (y-20) - (0.5f) / 2f + 0.5f;
		return new Vector3f(
			offX * cellSpacing,
			offY * cellSpacing,
			0
		);
	}

	private void addEventLogEntry(String s) {
		//add new log to the top, and limit to X rows
		
		this.logEntries.addFirst(s);
		if (this.logEntries.size() > EVENT_LOG_SIZE)
			this.logEntries.remove(EVENT_LOG_SIZE); //remove the last entry
		
		StringBuilder sb = new StringBuilder();
		for (String event: logEntries) {
			sb.append(event + "\n");
		}
		this.eventLog.setText(sb.toString());
	}

	@Override
	protected void cleanup(Application app) {
		((SimpleApplication)app).getRootNode().detachChild(rootNode);
		
		if (sounds != null)
			app.getStateManager().detach(sounds);
	}
	@Override
	protected void onEnable() {
		if (paused)
			pauseTimer = 3; //pause timer is always 3 long
		paused = false;
	}
	@Override
	protected void onDisable() { 
		paused = true;
		this.rootNode.attachChild(this.pausedText);
		
		//update once to show the paused state
		renderView();
	}
}
