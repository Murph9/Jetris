package base;

public class Record implements Comparable<Record> {
	private int score;
	private int lineCount;
	
	Record (int score, int lineCount) {
		this.score = score;
		this.lineCount = lineCount;
	}
	
	public int getScore() {
		return this.score; 
	}
	public int getLineCount() {
		return this.lineCount;
	}
	
	public String toString() { //for saving to file
		return (score + "\t" + lineCount);
	}
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!this.getClass().equals(o.getClass())) return false;
		if (o == this) return true;
		Record in = (Record) o;
		
		if (score == in.score && lineCount == in.lineCount) {
			return true;
		}
		return false;
	}
	
	public int compareTo(Record other){ 
		if (this.equals(other)) { return 0; }
		if (this.getScore() <= other.getScore()) { 
			return 1; // this is lower
		}
		return -2; // this is higher
	}
}
