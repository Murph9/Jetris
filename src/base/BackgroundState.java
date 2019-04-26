package base;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class BackgroundState extends BaseAppState {

	//http://guidohenkel.com/2018/05/endless_starfield_unity/
	//TODO particles are in front of everything, probably because this is added to GUI node first

	private static final int Z_DEPTH = -10;

	private Node rootGUINode;

	private Mesh mesh;
	private List<Vector3f> particles;
	private List<Float> particleParallax;
	private final int count;
	
	private int height;
	private int width;
	
	public BackgroundState(int particleCount) {
		this.count = particleCount;
	}
	
	@Override
	protected void initialize(Application app) {
		
		SimpleApplication sm = (SimpleApplication)app;
		
		rootGUINode = new Node("Background gui root node");
		sm.getGuiNode().attachChild(rootGUINode);
		
		height = sm.getCamera().getHeight();
		width = sm.getCamera().getWidth();
		
		particles = new ArrayList<Vector3f>(count);
		particleParallax = new ArrayList<Float>(count);
		for (int i = 0; i < count; i++) {
			Vector3f pos = H.randV3f(1, false);
			pos.y *= height;
			pos.x *= width;
			pos.z = Z_DEPTH;
			particles.add(pos);
			
			particleParallax.add(new Float(FastMath.nextRandomFloat()*30));
		}
		
		//star points mesh
		mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Points);
		mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(particles.toArray(new Vector3f[particles.size()])));
		mesh.updateCounts();
		mesh.updateBound();
		
		Geometry geo = new Geometry("particles", mesh);
		Material mat = new Material(sm.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.White);
		geo.setMaterial(mat);
		rootGUINode.attachChild(geo);


		//generate play field sized quad for the field background
		float xSize = CellHelper.fieldWidth(height, PlayState.X_SIZE/2, (PlayState.Y_SIZE-PlayState.Y_HIDDEN)/2)/2f;

		Mesh m = new Mesh();
		m.setBuffer(Type.Position, 3, new float[] {
				width/2f-xSize, 0, Z_DEPTH,
				width/2f+xSize, 0, Z_DEPTH,
				width/2f+xSize, height, Z_DEPTH,
				width/2f-xSize, height, Z_DEPTH
	        });
		m.setBuffer(Type.Index, 3, new short[]{0, 1, 2, 0, 2, 3});
		m.updateBound();
		m.setStatic();
		
		Geometry g = new Geometry("background", m);
		mat = new Material(sm.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", new ColorRGBA(1,1,1,0.08f));
		mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		g.setMaterial(mat);
		rootGUINode.attachChild(g);
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);

		for (int i = 0 ; i < particles.size(); i++) {
			Vector3f v = particles.get(i);
			v.x = (v.x + particleParallax.get(i)*tpf) % width;
		}
		
		mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(particles.toArray(new Vector3f[particles.size()])));
		mesh.updateBound();
	}
	
	@Override
	protected void cleanup(Application app) {
		SimpleApplication sm = (SimpleApplication)app;
		sm.getGuiNode().detachChild(rootGUINode);
	}

	@Override
	protected void onEnable() {}
	@Override
	protected void onDisable() {}
	

	class Star {
		Vector3f pos;
		float parallax;
		
		Star(Vector3f pos, float parallax) {
			this.pos = pos;
			this.parallax = parallax;
		}
	}
}
