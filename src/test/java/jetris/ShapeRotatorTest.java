package jetris;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jetris.logic.Shape;
import jetris.logic.ShapeRotator;
import jetris.logic.Shape.Type;

public class ShapeRotatorTest {

    Shape[] shapeList;

    @BeforeEach
    public void initTetrisGame() {
        shapeList = new Shape[Shape.Type.values().length - 1];
        int i = 0;
        for (Shape.Type type : Shape.Type.values()) {
            if (type == Type.O)
                continue; //not going to test O
            shapeList[i] = Shape.getNew(type);
            i++;
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0,1,2,3,4})
    public void RotateNone_KickCorrectly(int rotCount) {
        for (Shape s : shapeList) {
            float[] oldPos = s.getCentre();
            Shape newS = ShapeRotator.rotate(s, true, new ValidateDelay(rotCount));
            float[] diffPos = subArrays(newS.getCentre(), oldPos);
            float[] correctDiffPos = getKickPos(newS, rotCount);
            assertEquals(correctDiffPos[0], diffPos[0], s.getType() + " kick failed for x, count:" + rotCount);
            assertEquals(correctDiffPos[1], diffPos[1], s.getType() + " kick failed for y, count:" + rotCount);
        }
    }

    private float[] getKickPos(Shape s, int count) {
        Shape.Type t = s.getType();
        if (t == Type.J || t == Type.L || t == Type.S || t == Type.T || t == Type.Z)
            return kickData_JLSTZ[count];
        if (t == Type.I)
            return kickData_I[count];
        return new float[] { Float.MAX_VALUE, Float.MAX_VALUE };
    }

    //straight from https://harddrop.com/wiki/SRS#Wall_Kick_Illustration
    private static float[][] kickData_JLSTZ = new float[][] {
            new float[] { 0, 0 },
            new float[] { -1, 0 },
            new float[] { -1, 1 },
            new float[] { 0, -2 },
            new float[] { -1, -2 }
    };
    private static float[][] kickData_I = new float[][] {
            new float[] { 0, 0 },
            new float[] { -2, 0 },
            new float[] { 1, 0 },
            new float[] { -2, -1 },
            new float[] { 1, 2 }
    };

    class ValidateDelay implements Function<Shape, Boolean> {
        private final int count;
        private int i;
        ValidateDelay(int count) {
            this.count = count;
        }
        @Override
        public Boolean apply(Shape t) {
            boolean b = count == i;
            i += 1;
            return b;
        }
    }

    private static float[] subArrays(float[] one, float[] two) {
        if (one.length != two.length)
            return null;

        float[] out = new float[one.length];
        for (int i = 0; i < one.length; i++) {
            out[i] = one[i] - two[i];
        }
        return out;
    }
}
