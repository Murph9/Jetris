package base;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class BackgroundSpaceState extends BaseAppState {

	//http://guidohenkel.com/2018/05/endless_starfield_unity/
	//TODO particles are in front of everything, probably because this is added to GUI node first
	
	private Node rootNode;

	private Mesh mesh;
	private List<Vector3f> particles;
	private List<Float> particleParallax;
	private final int count;
	
	private int height;
	private int width;
	
	public BackgroundSpaceState(int particleCount) {
		this.count = particleCount;
	}
	
	@Override
	protected void initialize(Application app) {
		rootNode = new Node("Background space node");
		
		SimpleApplication sm = (SimpleApplication)app;
		sm.getViewPort().setBackgroundColor(ColorRGBA.Black);
		
		sm.getCamera().setLocation(new Vector3f());
		sm.getCamera().lookAt(new Vector3f(1,0,0), Vector3f.UNIT_Y);
		sm.getGuiNode().attachChild(rootNode); //TODO don't use gui node
		
		height = sm.getCamera().getHeight();
		width = sm.getCamera().getWidth();
		
		particles = new ArrayList<Vector3f>(count);
		particleParallax = new ArrayList<Float>(count);
		for (int i = 0; i < count; i++) {
			Vector3f pos = H.randV3f(1, false);
			pos.y *= height;
			pos.x *= width;
			pos.z = -10;
			particles.add(pos);
			
			particleParallax.add(new Float(FastMath.nextRandomFloat()*30));
		}
		
		mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Points);
		mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(particles.toArray(new Vector3f[particles.size()])));
		mesh.updateCounts();
		mesh.updateBound();
		
		Geometry geo = new Geometry("particles", mesh); // using our custom mesh object
		Material mat = new Material(sm.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.White);
		mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		geo.setMaterial(mat);
		rootNode.attachChild(geo);
	}

	@Override
	public void update(float tpf) {
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
		sm.getGuiNode().detachChild(rootNode);
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
