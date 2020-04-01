package jetris.base;

public class CellHelper {

	public static float cellSize(int screenHeight, int yCount) {
		return (screenHeight*0.95f)/yCount;
	}
	
	public static float cellMargin(int screenHeight, int yCount) {
		return (screenHeight*0.05f)/yCount;
	}
	
	public static float fieldWidth(int screenHeight, int xCount, int yCount) {
		return xCount*cellSize(screenHeight, yCount) + (xCount+1)*cellMargin(screenHeight, yCount);
	}
}
