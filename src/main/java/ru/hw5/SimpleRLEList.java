package ru.hw5;

import org.apache.http.annotation.NotThreadSafe;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author evans
 *         06.04.14.
 */
@NotThreadSafe
public class SimpleRLEList<T> implements RLEList<T>, Iterable<T> {
    private final ArrayList<Pair<Integer, T>> list = new ArrayList<>();

    public ArrayList<Pair<Integer, T>> getList() {
        return list;
    }

    @Override
    public void append(T value) {
        if (!list.isEmpty()) {
            Pair<Integer, T> pair = list.get(list.size() - 1);
            if (nullEquals(pair.snd, value)) {
                list.set(list.size() - 1, Pair.of(pair.fst + 1, pair.snd));
                return;
            }
        }
        list.add(Pair.of(1, value));
    }

    private boolean nullEquals(T a, T b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    @Override
    public void insert(int index, T value) {
        if(list.isEmpty()){
            append(value);
            return;
        }
        int innerIndex = 0;
        int currIndex = 0;
        Pair<Integer, T> elem = list.get(currIndex);
        while (innerIndex + elem.fst < index && currIndex < list.size()) {
            innerIndex += elem.fst;
            elem = list.get(++currIndex);
        }

        if (nullEquals(elem.snd, value)) {
            list.set(currIndex, Pair.of(elem.fst + 1, elem.snd));
        } else if(innerIndex + elem.fst == index && currIndex + 1 < list.size() && nullEquals(list.get(currIndex + 1).snd, value)){
            list.set(currIndex + 1, Pair.of(list.get(currIndex + 1).fst + 1, value));
        } else {
            Pair<Integer, T> fst = Pair.of(index - innerIndex, elem.snd);
            Pair<Integer, T> snd = Pair.of(elem.fst - fst.fst, elem.snd);
            list.set(currIndex, fst);
            list.add(currIndex + 1, Pair.of(1, value));
            list.add(currIndex + 2, snd);
        }
    }

    @Override
    public T get(int index) {
        int innerIndex = 0;
        int currIndex = 0;
        Pair<Integer, T> elem = list.get(currIndex);
        while (innerIndex + elem.fst <= index) {
            innerIndex += elem.fst;
            elem = list.get(++currIndex);
        }
        return elem.snd;
    }

    //todo check for remove on last elem

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = -1;
            private Pair<Integer, T> elem = null;
            private int used = 0;
            private boolean alreadyRemovedBefore = false;

            @Override
            public boolean hasNext() {
                return (elem != null && used < elem.fst) || index + 1 < list.size();
            }

            @Override
            public T next() {
                if (elem == null || used == elem.fst) {
                    elem = list.get(++index);
                    used = 1;
                } else {
                    ++used;
                }
                alreadyRemovedBefore = false;
                return elem.snd;
            }

            @Override
            public void remove() {
                if(alreadyRemovedBefore){
                    throw new IllegalStateException("Remove was already called for this elem");
                }
                alreadyRemovedBefore = true;
                if (elem.fst > 1) {
                    elem = Pair.of(elem.fst - 1, elem.snd);
                    list.set(index, elem);
                    --used;
                } else {
                    list.remove(index);
                    elem = null;
                    --index;
                }
            }
        };
    }
}

