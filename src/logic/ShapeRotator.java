package logic;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import logic.Shape.Rotation;

public class ShapeRotator {

	//Whole reason this class exists:
	//https://harddrop.com/wiki/SRS#Wall_Kick_Illustration
	//http://tetris.wikia.com/wiki/SRS
	
	enum KickType {
		NORMAL,
		OTHER;
	}
	
	public static Shape rotate(Shape shape, boolean right, Function<Shape, Boolean> isValid) {
		Shape.Rotation rotState = shape.rotState;
		shape.rotate(right);
		List<Kick> kicks = kickData(shape.kickType, rotState, shape.rotState);
		for (Kick k: kicks) {
			Shape s = shape.clone();
			s.translate(k.x, -k.y); //my y is inverted from all guides online
			if (isValid.apply(s)) {
				return s;
			}
		}
	
		return shape;
	}
	
	private static List<Kick> kickData(KickType type, Shape.Rotation rot, Shape.Rotation newRot) {
		if (type == KickType.OTHER) {
			if (rot == Rotation.NONE && newRot == Rotation.RIGHT)
				return kickI0_R;
			if (rot == Rotation.RIGHT && newRot == Rotation.NONE)
				return kickIR_0;
			
			if (rot == Rotation.RIGHT && newRot == Rotation.DOUBLE)
				return kickIR_2;
			if (rot == Rotation.DOUBLE && newRot == Rotation.RIGHT)
				return kickI2_R;
			
			if (rot == Rotation.DOUBLE && newRot == Rotation.LEFT)
				return kickI2_L;
			if (rot == Rotation.LEFT && newRot == Rotation.DOUBLE)
				return kickIL_2;
			
			if (rot == Rotation.NONE && newRot == Rotation.LEFT)
				return kickI0_L;
			if (rot == Rotation.LEFT && newRot == Rotation.NONE)
				return kickIL_0;
		} else {
			if (rot == Rotation.NONE && newRot == Rotation.RIGHT)
				return kick0_R;
			if (rot == Rotation.RIGHT && newRot == Rotation.NONE)
				return kickR_0;
			
			if (rot == Rotation.RIGHT && newRot == Rotation.DOUBLE)
				return kickR_2;
			if (rot == Rotation.DOUBLE && newRot == Rotation.RIGHT)
				return kick2_R;
			
			if (rot == Rotation.DOUBLE && newRot == Rotation.LEFT)
				return kick2_L;
			if (rot == Rotation.LEFT && newRot == Rotation.DOUBLE)
				return kickL_2;
			
			if (rot == Rotation.NONE && newRot == Rotation.LEFT)
				return kick0_L;
			if (rot == Rotation.LEFT && newRot == Rotation.NONE)
				return kickL_0;
		}
	
		try {
			throw new Exception("Unknown section of kickData");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/////REGION of kick constants
	//TODO the first one is always 0,0
	//TODO a_b == b_a * -1
	private static final List<Kick> kick0_R = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(-1,1), new Kick(0,-2), new Kick(-1,-2)
		);
	private static final List<Kick> kickR_0 = Arrays.asList(
			 new Kick(0,0), new Kick(1,0), new Kick(1,-1), new Kick(0,2), new Kick(1,2)
		);
	
	private static final List<Kick> kickR_2 = Arrays.asList(
			 new Kick(0,0), new Kick(1,0), new Kick(1,-1), new Kick(0,2), new Kick(1,2)
		);
	private static final List<Kick> kick2_R = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(-1,1), new Kick(0,-2), new Kick(-1,-2)
		);
	
	private static final List<Kick> kick2_L = Arrays.asList(
			 new Kick(0,0), new Kick(1,0), new Kick(1,1), new Kick(0,-2), new Kick(1,-2)
		);
	private static final List<Kick> kickL_2 = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(-1,-1), new Kick(0,2), new Kick(-1,2)
		);
	
	private static final List<Kick> kickL_0 = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(-1,-1), new Kick(0,2), new Kick(-1,2)
		);
	private static final List<Kick> kick0_L = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(1,1), new Kick(0,-2), new Kick(1,-2)
		);
	
	
	//kick constants for the line 'I' (and technically 'O' but it doesn't change)
	private static final List<Kick> kickI0_R = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(-1,1), new Kick(0,-2), new Kick(-1,-2)
		);
	private static final List<Kick> kickIR_0 = Arrays.asList(
			 new Kick(0,0), new Kick(1,0), new Kick(1,-1), new Kick(0,2), new Kick(1,2)
		);
	
	private static final List<Kick> kickIR_2 = Arrays.asList(
			 new Kick(0,0), new Kick(1,0), new Kick(1,-1), new Kick(0,2), new Kick(1,2)
		);
	private static final List<Kick> kickI2_R = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(-1,1), new Kick(0,-2), new Kick(-1,-2)
		);
	
	private static final List<Kick> kickI2_L = Arrays.asList(
			 new Kick(0,0), new Kick(1,0), new Kick(1,1), new Kick(0,-2), new Kick(1,-2)
		);
	private static final List<Kick> kickIL_2 = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(-1,-1), new Kick(0,2), new Kick(-1,2)
		);
	
	private static final List<Kick> kickIL_0 = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(-1,-1), new Kick(0,2), new Kick(-1,2)
		);
	private static final List<Kick> kickI0_L = Arrays.asList(
			 new Kick(0,0), new Kick(-1,0), new Kick(1,1), new Kick(0,-2), new Kick(1,-2)
		);
}
