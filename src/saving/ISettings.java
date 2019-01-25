package saving;

public interface ISettings {
	/** Uses a random colour for each piece. */
    boolean randomColours();
    /** Show a ghost shape */
    boolean ghost();
    /** (A surprise) Locked cells aren't shown anymore (the ghost and current are) */
    boolean expertMode();
    /** Does hard drop lock by default? */
    boolean hardDropLock();
    /** No field colors, only grey. */
    boolean greyScale();
}
