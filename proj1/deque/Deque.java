package deque;

/** A double-ended queue (a generalization of a list, stack and queue). */
public interface Deque<Item> {

    /** Adds an item to the front of the deque. */
    void addFirst(Item x);

    /** Adds an item to the back of the deque. */
    void addLast(Item x);

    /** Returns true if the deque contains no items. */
    default boolean isEmpty() {
        return size() == 0;
    }

    /** Returns the number of items in the deque. */
    int size();

    /** Prints the items in the deque from first to last. */
    void printDeque();

    /** Removes and returns the item at the front, or null if empty. */
    Item removeFirst();

    /** Removes and returns the item at the back, or null if empty. */
    Item removeLast();

    /** Gets the item at the given index (0 is the front), or null if invalid. */
    Item get(int index);
}
