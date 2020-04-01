package jetris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jetris.logic.Tetris;
import jetris.logic.TetrisGame;

public class TetrisGameTest {

    private Tetris t;
    @BeforeEach
    public void initTetrisGame() {
        t = new TetrisGame(10, 20, 3, null);
        t.initialise();
    }

    @Test
    public void InitialScore_ShouldBeTheDefault() {
        assertEquals(0, t.getScore());
        assertEquals(1, t.getLevel());
        assertEquals(0, t.getLines().size());
    }

    @Test
    public void InitialLines_ShouldBeNone() {
        assertFalse(t.newLine());
    }
}

