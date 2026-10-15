package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    /** Simple comparison test: add three values to both lists, then remove
     *  them one at a time and verify the results (and sizes) match. */
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();

        correct.addLast(4);
        correct.addLast(5);
        correct.addLast(6);
        buggy.addLast(4);
        buggy.addLast(5);
        buggy.addLast(6);

        assertEquals(correct.size(), buggy.size());
        assertEquals(correct.removeLast(), buggy.removeLast());
        assertEquals(correct.removeLast(), buggy.removeLast());
        assertEquals(correct.removeLast(), buggy.removeLast());
        assertEquals(correct.size(), buggy.size());
    }

    /** Randomized comparison test: make random calls to both implementations
     *  and verify that they always agree. */
    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                buggy.addLast(randVal);
            } else if (operationNumber == 1) {
                // getLast
                if (correct.size() > 0) {
                    assertEquals(correct.getLast(), buggy.getLast());
                }
            } else if (operationNumber == 2) {
                // removeLast
                if (correct.size() > 0) {
                    assertEquals(correct.removeLast(), buggy.removeLast());
                }
            } else if (operationNumber == 3) {
                // size
                assertEquals(correct.size(), buggy.size());
            }
        }
    }
}
