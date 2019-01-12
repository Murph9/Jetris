package logic;

public class CellColour {

	public final float r;
	public final float g;
	public final float b;
	public final float a;
	
	public CellColour(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	@Override
	public String toString() {
		return "CC: r:" + r + ", g:" + g + ", b:" + b + ", a:" + a;
	}
}
