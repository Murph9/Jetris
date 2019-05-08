package logic;

import java.util.Arrays;
import java.util.List;

public class ScoringSystem {

    private int level;
    private int levelPoints;

	private int lineCombo; //for scoring, 1 per combo

    private int lines;
    private long score;

    private Action lastAction; //for B2B detection

    ScoringSystem() {
        this.lastAction = Action.NOTHING;
        
		this.level = 1;
    }

	int getLinesCount() { return lines; }
	int getLevel() { return level; }
	int getLines() { return lines; }
    long getScore() { return score; }
    int getLineCombo() { return lineCombo; }

    /** Call for piece drop distance */
    void doPieceDrop(int count) {
        this.score += count;
    }

    /** Call for every piece lock */
    void doAction(Action ac) {
        //inc score as per action points
        if (this.lastAction == ac && ac.canB2B) {
            //B2B scoring is 1.5 times more
            this.score += 1.5f * ac.mult * this.level;
            this.levelPoints += 1.5f * (ac.mult/100);
        } else {
            this.score += ac.mult * level;
            this.levelPoints += ac.mult/100;
        }

        //add to the line combo
        if (Action.NOT_A_LINE.contains(ac)) {
            this.lineCombo = 0; //no lines, then no line combo
        } else {
            this.lastAction = ac;
            this.score += 50 * this.lineCombo * this.level; //first line has a lineCombo of 0
            this.lineCombo++;
        }

        updateLevel();
    }

	private void updateLevel() {
		//update level based on levelPoints
		while (this.levelPoints > this.level*5) {
			this.levelPoints -= this.level*5;
			this.level++;
		}
		
		this.level = Math.min(this.level, 20); //prevent a higher level than 20
	}

    @Override
    public ScoringSystem clone() {
        ScoringSystem ss = new ScoringSystem();
        ss.level = this.level;
		ss.levelPoints = this.levelPoints;
		ss.score = this.score;
		ss.lines = this.lines;
        ss.lineCombo = this.lineCombo;
        ss.lastAction = this.lastAction;
        return ss;
    }

    enum Action { //scoring types
        //https://tetris.fandom.com/wiki/Scoring#Guideline_scoring_system
        
        NOTHING(0, false), //default value
        SINGLE(100, false),
        T_SPIN_MINI(100, true),
        T_SPIN_MINI_SINGLE(200, false), //'half' a t-spin with a line
        DOUBLE(300, false),
        T_SPIN(400, false), //a t-spin with no lines
        TRIPLE(500, false),
        TETRIS(800, true),
        T_SPIN_SINGLE(800, true),
        T_SPIN_DOUBLE(1200, true),
        T_SPIN_TRIPLE(1600, true);

        private int mult;
        private boolean canB2B;
        private Action(int mult, boolean canB2B) {
            this.mult = mult;
            this.canB2B = canB2B;
        }

        public static List<Action> NOT_A_LINE = Arrays.asList(Action.NOTHING, Action.T_SPIN, Action.T_SPIN_MINI);
    }
}