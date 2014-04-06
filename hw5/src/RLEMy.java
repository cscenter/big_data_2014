/**
 *
 * Elizaveta Shashkova, CSC, 2014
 *
 */

import java.util.*;

class Rep_object<T> {
    int first_ind;
    T val;
    int rep;
    Rep_object(int first_index, T value, int repet) {
        first_ind = first_index;
        val = value;
        rep = repet;
    }
}



public class RLEMy<T> implements RLEList<T> {
    private LinkedList<Rep_object<T>> myImpl;
    private static int full_length;

    RLEMy() {
        myImpl = new LinkedList<Rep_object<T>>();
        full_length = 0;
    }

    public int getFull_length() {
        return full_length;
    }

    public void show() {
        ListIterator<Rep_object<T>> it = myImpl.listIterator();
        while (it.hasNext()) {
            Rep_object<T> obj = it.next();
            for (int j = 0; j < obj.rep; ++j) {
                System.out.print(obj.val + " ");
            }
        }
        System.out.println();
    }

    public void append(T value) {
        if (full_length == 0) {
            Rep_object<T> obj = new Rep_object<T>(0, value, 1);
            myImpl.addLast(obj);
            full_length++;
            return;
        }
        Rep_object<T> last_obj = myImpl.getLast();
        if (last_obj.val == value) {
            last_obj.rep += 1;
            myImpl.removeLast();
        } else {
            last_obj = new Rep_object<T>(full_length, value, 1);
        }
        myImpl.addLast(last_obj);
        full_length++;
    }

    public void insert(int index, T value) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > full_length - 1)) {
            throw new IndexOutOfBoundsException();
        }
        int curr_ind = 0;
        ListIterator<Rep_object<T>> it = myImpl.listIterator();
        //int i = 0;
        while (it.hasNext()) {
            Rep_object<T> obj = it.next();
            if (curr_ind + obj.rep - 1 < index) {
                curr_ind += obj.rep;
            } else {
                if (index == curr_ind) {
                    it.previous();
                    if (it.hasPrevious()) {
                        Rep_object<T> prev = it.previous();
                        if (prev.val == value) {
                            prev.rep += 1;
                            it.set(prev);
                            if (obj.rep == 1) {
                                it.next();
                                it.next();
                                it.remove();
                            } else {
                                obj.rep -= 1;
                            }
                            break;
                        }
                        it.next();
                        it.next();
                    }
                }
                if (index == curr_ind + obj.rep - 1) {
                    if (it.hasNext()) {
                        Rep_object<T> next = it.next();
                        if (next.val == value) {
                            next.rep += 1;
                            it.set(next);
                            if (obj.rep == 1) {
                                it.previous();
                                it.remove();
                            } else {
                                obj.rep -= 1;
                            }
                            break;
                        }
                        it.previous();
                    }
                }
                if (obj.val != value) {
                    it.remove();
                    if (index - curr_ind != 0) {
                        Rep_object<T> prevObj = new Rep_object<T>(curr_ind, obj.val, index - curr_ind);
                        it.add(prevObj);
                    }
                    Rep_object<T> center = new Rep_object<T>(index, value, 1);
                    it.add(center);
                    if (curr_ind + obj.rep - 1 - index != 0) {
                        Rep_object<T> nextObj = new Rep_object<T>(index + 1, obj.val, curr_ind + obj.rep -1 - index);
                        it.add(nextObj);
                    }
                }
                break;
            }
        }
    }

    public T get(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > full_length - 1)) {
            throw new IndexOutOfBoundsException();
        }
        int curr_ind = 0;
        ListIterator<Rep_object<T>> it = myImpl.listIterator();
        while (it.hasNext()) {
            Rep_object<T> obj = it.next();
            if (curr_ind + obj.rep - 1 < index) {
                curr_ind += obj.rep;
            } else {
                return obj.val;
            }
        }
        return null;
    }

    private class RLEMyIterator implements Iterator {
        ListIterator<Rep_object<T>> inner_iterator;
        Rep_object<T> cur_object;
        int obj_index;

        RLEMyIterator() {
            inner_iterator = myImpl.listIterator();
            cur_object = inner_iterator.next();
            obj_index = 0;
        }

        @Override
        public void remove() {
            if (cur_object.rep > 1) {
                cur_object.rep -= 1;
                obj_index -= 1;
                inner_iterator.set(cur_object);
            } else {
                inner_iterator.remove();
                cur_object = inner_iterator.next();
                obj_index = 0;
            }
        }

        @Override
        public T next() {
            if (obj_index < cur_object.rep) {
                obj_index++;
                return cur_object.val;
            } else {
                cur_object = inner_iterator.next();
                obj_index = 1;
                return cur_object.val;
            }
        }

        @Override
        public boolean hasNext() {
            if (obj_index < cur_object.rep) {
                return true;
            } else {
                if (inner_iterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }
    }

    public Iterator<T> iterator() {
        return new RLEMyIterator();
    }
}
