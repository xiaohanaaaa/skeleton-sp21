package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    /** Tests a list with more than one prime, exposing the fact that
     *  squarePrimes must keep visiting the rest of the list even after
     *  squaring a prime. */
    @Test
    public void testSquarePrimesMultiple() {
        IntList lst = IntList.of(2, 3, 4, 5, 6);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 9 -> 4 -> 25 -> 6", lst.toString());
        assertTrue(changed);
    }

    /** Tests a list with no primes, which should be left alone. */
    @Test
    public void testSquarePrimesNone() {
        IntList lst = IntList.of(4, 6, 8, 9, 10);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 6 -> 8 -> 9 -> 10", lst.toString());
        assertFalse(changed);
    }

    /** Tests that an empty list reports that nothing changed. */
    @Test
    public void testSquarePrimesEmpty() {
        assertFalse(IntListExercises.squarePrimes(null));
    }
}
