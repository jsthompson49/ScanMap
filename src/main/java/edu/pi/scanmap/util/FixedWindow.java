package main.java.edu.pi.scanmap.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FixedWindow<T> {
    private final T[] buffer;
    private int index = 0;

    public FixedWindow(Class<T> clazz, final int size) {
        buffer = (T[]) Array.newInstance(clazz, Math.max(1, size));
    }

    public synchronized void add(final T item) {
        buffer[index] = item;
        if (index == buffer.length - 1) {
            index = 0;
        } else {
            index++;
        }
    }

    public synchronized List<T> getItems() {
        final List<T> items = new ArrayList<>(buffer.length);

        int reverseIndex = index - 1;
        for (int i = 0;i < buffer.length; i++) {
            if (reverseIndex < 0) {
                reverseIndex = buffer.length - 1;
            }
            items.add(buffer[reverseIndex]);
            reverseIndex--;
        }

        return  items;
    }
}
