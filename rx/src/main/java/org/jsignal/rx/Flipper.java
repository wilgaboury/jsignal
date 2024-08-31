package org.jsignal.rx;

import java.util.function.Supplier;

public class Flipper<T> {
    private T front;
    private T back;

    public Flipper(Supplier<T> create) {
        this.front = create.get();
        this.back = create.get();
    }

    public T getFront() {
        return front;
    }

    public T getBack() {
        return back;
    }

    public void flip() {
        var tmp = front;
        front = back;
        back = tmp;
    }
}
