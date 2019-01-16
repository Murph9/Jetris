package logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Generator {
	//http://tetris.wikia.com/wiki/Random_Generator
	
	private static Random rand = new Random();
	
	private List<Shape.Type> currentBag;
	private final Shape.Type[] baseTypes;
	public Generator(Shape.Type[] types) {
		this.baseTypes = types;
	}
	
	public Shape next() {
		//if null set as empty and return something other than (S or Z)
		if (this.currentBag == null) {
			this.currentBag = Collections.emptyList(); //NOTE: creates an immutable empty list
			
			Shape.Type t = Shape.Type.S;
			while (t == Shape.Type.S || t == Shape.Type.Z) {
				//this could technically never end
				t = baseTypes[rand.nextInt(this.baseTypes.length)];
			}
			return Shape.getNew(t);
		}
	
		if (this.currentBag.isEmpty()) //only reset when asked
			this.currentBag = new LinkedList<Shape.Type>(Arrays.asList(baseTypes));
		

		int index = rand.nextInt(this.currentBag.size());
		Shape.Type t = this.currentBag.remove(index);
		
		return Shape.getNew(t);
	}
}
