package deque;

/** A Deque implemented with a resizing circular array. */
public class ArrayDeque<Item> implements Deque<Item> {

    /** Initial capacity, and the capacity below which we stop shrinking. */
    private static final int MIN_CAPACITY = 8;

    private Item[] items;
    private int size;
    /** Physical index just before the front; next addFirst writes here. */
    private int nextFirst;
    /** Physical index just after the back; next addLast writes here. */
    private int nextLast;

    public ArrayDeque() {
        items = (Item[]) new Object[MIN_CAPACITY];
        size = 0;
        nextFirst = 0;
        nextLast = 1;
    }

    @Override
    public void addFirst(Item x) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextFirst] = x;
        nextFirst = (nextFirst - 1 + items.length) % items.length;
        size += 1;
    }

    @Override
    public void addLast(Item x) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextLast] = x;
        nextLast = (nextLast + 1) % items.length;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i += 1) {
            System.out.print(items[(nextFirst + 1 + i) % items.length] + " ");
        }
        System.out.println();
    }

    @Override
    public Item removeFirst() {
        if (size == 0) {
            return null;
        }
        nextFirst = (nextFirst + 1) % items.length;
        Item x = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        maybeShrink();
        return x;
    }

    @Override
    public Item removeLast() {
        if (size == 0) {
            return null;
        }
        nextLast = (nextLast - 1 + items.length) % items.length;
        Item x = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        maybeShrink();
        return x;
    }

    @Override
    public Item get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return items[(nextFirst + 1 + index) % items.length];
    }

    /** Halves the array when usage drops below 25%, but never below MIN_CAPACITY. */
    private void maybeShrink() {
        if (items.length > MIN_CAPACITY && size < items.length / 4) {
            resize(items.length / 2);
        }
    }

    /** Resizes to the given capacity, reordering items so front is at index 0. */
    private void resize(int capacity) {
        Item[] newItems = (Item[]) new Object[capacity];
        for (int i = 0; i < size; i += 1) {
            newItems[i] = items[(nextFirst + 1 + i) % items.length];
        }
        items = newItems;
        nextFirst = capacity - 1;
        nextLast = size;
    }
}
