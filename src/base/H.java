package base;

import java.util.Random;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import logic.CellColour;

public class H {
	
	private static Random rand = new Random();
	
	public static CellColour randomColourHSV() {
		float[] rgb = hsvToRGB(rand.nextInt(360), (rand.nextFloat()/2)+0.3, (rand.nextDouble()/2)+0.3);
		return new CellColour(rgb[0], rgb[1], rgb[2], 1); 
	}
	
	//0 <= h < 360, 0 <= s< 1, 0 <= v< 1, type 
	public static float[] hsvToRGB(int h, double s, double v) {
		float[] out = new float[3];
		double[] temp = new double[3];
		double c = v*s;
		int tempH = h/60;
		double x = c*(1-Math.abs(tempH%2 - 1));
		double m = v-c;
		if ( tempH < 1) {
			temp[0] = c;
			temp[1] = x;
			temp[2] = 0;
		} else if (tempH < 2) {
			temp[0] = x;
			temp[1] = c;
			temp[2] = 0;
		} else if (tempH < 3) {
			temp[0] = 0;
			temp[1] = c;
			temp[2] = x;
		} else if (tempH < 4) {
			temp[0] = 0;
			temp[1] = x;
			temp[2] = c;
		} else if (tempH < 5) {
			temp[0] = x;
			temp[1] = 0;
			temp[2] = c;
		} else if (tempH < 6) {
			temp[0] = c;
			temp[1] = 0;
			temp[2] = x;
		} else { //just in case
			temp[0] = 0;
			temp[1] = 0;
			temp[2] = 0;
		}
		out[0] = (float)(temp[0] + m);
		out[1] = (float)(temp[1] + m);
		out[2] = (float)(temp[2] + m);
		return out;
	}
	
	
	public static Vector3f randV3f(float max, boolean scaleNegative) {
		float offset = scaleNegative ? max : 0;
		float scale = scaleNegative ? 2 : 1;
		return new Vector3f(FastMath.nextRandomFloat()*scale*max-offset, FastMath.nextRandomFloat()*scale*max-offset, FastMath.nextRandomFloat()*scale*max-offset);
	}
}
