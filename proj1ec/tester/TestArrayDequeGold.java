package tester;

import static org.junit.Assert.*;

import org.junit.Test;

import student.StudentArrayDeque;
import edu.princeton.cs.introcs.StdRandom;

/** Randomly compares the buggy StudentArrayDeque against the correct
 *  ArrayDequeSolution.  As soon as the two disagree, the JUnit failure
 *  message prints the sequence of operations that reproduces the bug. */
public class TestArrayDequeGold {
    @Test
    public void testDeque() {
        StudentArrayDeque<Integer> student = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> solution = new ArrayDequeSolution<>();
        StringBuilder history = new StringBuilder();

        while (true) {
            double choice = StdRandom.uniform();
            int value = StdRandom.uniform(100);

            if (choice < 0.25) {
                history.append("addFirst(").append(value).append(")\n");
                student.addFirst(value);
                solution.addFirst(value);
            } else if (choice < 0.5) {
                history.append("addLast(").append(value).append(")\n");
                student.addLast(value);
                solution.addLast(value);
            } else if (choice < 0.75 && solution.size() > 0) {
                history.append("removeFirst()\n");
                Integer expected = solution.removeFirst();
                Integer actual = student.removeFirst();
                assertEquals(history.toString(), expected, actual);
            } else if (solution.size() > 0) {
                history.append("removeLast()\n");
                Integer expected = solution.removeLast();
                Integer actual = student.removeLast();
                assertEquals(history.toString(), expected, actual);
            } else {
                history.append("addLast(").append(value).append(")\n");
                student.addLast(value);
                solution.addLast(value);
            }
        }
    }
}
