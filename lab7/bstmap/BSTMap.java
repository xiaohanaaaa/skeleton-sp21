package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/** A Map61B implemented as a binary search tree. Keys must be Comparable. */
public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private BSTNode root;
    private int size;

    /** One node of the BST: a key-value pair plus left/right subtrees. */
    private class BSTNode {
        K key;
        V value;
        BSTNode left;
        BSTNode right;

        BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    /** Removes every mapping by dropping the root and resetting size. */
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    /** Returns true if a node with KEY exists. Delegates to the recursive helper. */
    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(BSTNode node, K key) {
        if (node == null) {
            return false;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return containsKey(node.left, key);
        } else if (cmp > 0) {
            return containsKey(node.right, key);
        }
        return true;
    }

    /** Returns the value mapped to KEY, or null if KEY is absent. */
    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        }
        return node.value;
    }

    /** Returns the number of key-value mappings. */
    @Override
    public int size() {
        return size;
    }

    /** Inserts or updates KEY -> VALUE, incrementing size only on new keys. */
    @Override
    public void put(K key, V value) {
        int oldSize = size;
        root = put(root, key, value);
        if (size == oldSize) {
            size++;
        }
    }

    private BSTNode put(BSTNode node, K key, V value) {
        if (node == null) {
            return new BSTNode(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    /** Returns a Set of all keys (built from the iterator). */
    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (K key : this) {
            keys.add(key);
        }
        return keys;
    }

    /** Removes KEY, returning its old value (or null if absent). */
    @Override
    public V remove(K key) {
        V old = get(key);
        if (old != null || containsKey(key)) {
            root = remove(root, key);
            size--;
        }
        return old;
    }

    /** Removes KEY only if it maps to VALUE; returns old value or null. */
    @Override
    public V remove(K key, V value) {
        V old = get(key);
        if (old != null && old.equals(value)) {
            root = remove(root, key);
            size--;
            return old;
        }
        return null;
    }

    private BSTNode remove(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            BSTNode successor = min(node.right);
            node.key = successor.key;
            node.value = successor.value;
            node.right = remove(node.right, successor.key);
        }
        return node;
    }

    private BSTNode min(BSTNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    /** Returns an in-order (sorted) iterator over the keys. */
    @Override
    public Iterator<K> iterator() {
        return new BSTMapIter();
    }

    /** In-order traversal using an explicit stack of pending nodes. */
    private class BSTMapIter implements Iterator<K> {
        private Stack<BSTNode> stack = new Stack<>();

        BSTMapIter() {
            pushLeft(root);
        }

        private void pushLeft(BSTNode node) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public K next() {
            BSTNode node = stack.pop();
            pushLeft(node.right);
            return node.key;
        }
    }
}
