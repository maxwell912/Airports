package airport.info;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
class PrefixTree<T> implements PrefixMap<String, T> {

    String key;
    boolean hasValue = false;
    T value;

    PrefixTree<T> subTree;
    PrefixTree<T> neighbourTree;

    PrefixTree() {}

    private PrefixTree(String key, T value) {
        this.key = key;
        this.setValue(value);
    }

    private PrefixTree(String key) {
        this.key = key;
    }

    private void setValue(T value) {
        hasValue = true;
        this.value = value;
    }

    public void add(String newKey, T newValue) {
        if (key == null) {
            key = newKey;
            setValue(newValue);
            return;
        }

        var commonPrefixLength = getCommonPrefixLength(newKey);
        var keyLength = key.length();
        var newKeyLength = newKey.length();

        if (commonPrefixLength == 0) {
            this.addToCurrentTree(newKey, newValue);

        } else if (keyLength > commonPrefixLength && newKeyLength == commonPrefixLength) {
            splitNode(commonPrefixLength);
            this.setValue(newValue);

        } else if (keyLength >= commonPrefixLength && newKeyLength >= commonPrefixLength) {
            if (keyLength > commonPrefixLength)
                splitNode(commonPrefixLength);
            this.addToSubTree(newKey.substring(commonPrefixLength), newValue);
        }
    }

    public Iterable<T> getByPrefix(String prefix) {
        if (key == null)
            return new ArrayList<>();

        var length = getCommonPrefixLength(prefix);
        if (length == 0) {
            if (neighbourTree != null)
                return neighbourTree.getByPrefix(prefix);

        } else if (key.length() == length && prefix.length() > length){
            if (subTree != null)
                return subTree.getByPrefix(prefix.substring(length));
        } else if (key.length() >= length && prefix.length() == length) {
            return new PrefixTreeIterator<>(this);
        }
        return new ArrayList<>();
    }

    private void addToSubTree(String newKey, T newValue) {
        if (subTree == null) {
            subTree = new PrefixTree<>(newKey, newValue);
        } else if (subTree.getCommonPrefixLength(newKey) == 0 && subTree.key.compareTo(newKey) > 0) {
            var newNode = new PrefixTree<>(newKey, newValue);
            newNode.neighbourTree = this.subTree;
            this.subTree = newNode;
        } else
            subTree.add(newKey, newValue);
    }

    private void addToCurrentTree(String newKey, T newValue) {
        if (neighbourTree == null) {
            neighbourTree = new PrefixTree<>(newKey, newValue);
        } else if (neighbourTree.key.compareTo(newKey) >= 0 && neighbourTree.getCommonPrefixLength(newKey) == 0) {
            var newNode = new PrefixTree<>(newKey, newValue);
            newNode.neighbourTree = this.neighbourTree;
            this.neighbourTree = newNode;
        } else
            neighbourTree.add(newKey, newValue);
    }

    private void splitNode(Integer prefixLength) {
        var start = key.substring(0, prefixLength);
        var end = key.substring(prefixLength);

        PrefixTree<T> newNode = new PrefixTree<>(end);
        newNode.subTree = this.subTree;

        subTree = newNode;
        key = start;
        if (hasValue) {
            newNode.setValue(value);
            hasValue = false;
            value = null;
        }
    }

    private Integer getCommonPrefixLength(String prefix) {
        var length = Math.min(prefix.length(), key.length());
        var count = 0;
        for (var i = 0; i < length; i++) {
            if (prefix.charAt(i) == key.charAt(i)) {
                count++;
            } else break;
        }
        return count;
    }

    static class PrefixTreeIterator<T> implements Iterable<T>, Iterator<T> {

        Stack<PrefixTree<T>> nodeStack;
        ArrayDeque<T> results;

        PrefixTreeIterator(PrefixTree<T> tree) {
            nodeStack = new Stack<>();
            results = new ArrayDeque<>();

            PrefixTree<T> currentNode;
            if (tree.hasValue)
                results.add(tree.value);
            if (tree.subTree != null)
                currentNode = tree.subTree;
            else
                return;

            while (true) {
                while (true) {
                    if (currentNode.neighbourTree != null)
                        nodeStack.push(currentNode);
                    if (currentNode.hasValue)
                        results.add(currentNode.value);
                    if (currentNode.subTree == null)
                        break;
                    currentNode = currentNode.subTree;
                }
                if (nodeStack.empty())
                    break;
                currentNode = nodeStack.pop().neighbourTree;
            }
        }

        @Override
        public boolean hasNext() {
            return !results.isEmpty();
        }

        @Override
        public T next() {
            return results.poll();
        }

        @Override
        public Iterator<T> iterator() {
            return this;
        }
    }
}

