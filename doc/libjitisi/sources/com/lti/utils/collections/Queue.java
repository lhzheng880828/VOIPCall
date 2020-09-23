package com.lti.utils.collections;

import java.util.ArrayList;
import java.util.List;

public class Queue<T> {
    private List<T> v = new ArrayList();

    public T dequeue() {
        T o = this.v.get(0);
        this.v.remove(0);
        return o;
    }

    public void enqueue(T o) {
        this.v.add(o);
    }

    public boolean isEmpty() {
        return this.v.size() == 0;
    }

    public T peek() {
        if (this.v.size() == 0) {
            return null;
        }
        return this.v.get(0);
    }

    public void removeAllElements() {
        this.v.clear();
    }

    public int size() {
        return this.v.size();
    }
}
