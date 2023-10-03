package com.github.wilgaboury.jsignal.state;

import com.github.wilgaboury.jsignal.interfaces.Signal;

import java.util.Collection;
import java.util.Map;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class StateMachine<S> {
    private final Map<S, ? extends Collection<S>> transitions;
    private final Signal<S> cur;
    private final Signal<S> prev;

    public StateMachine(Map<S, ? extends Collection<S>> transitions, Signal<S> cur, Signal<S> prev) {
        this.transitions = transitions;
        this.cur = cur;
        this.prev = prev;
    }

    public Collection<S> getChoices() {
        return transitions.get(cur.get());
    }

    public S getCur() {
        return cur.get();
    }

    public S getPrev() {
        return prev.get();
    }

    public boolean isIn(S state) {
        return cur.get() == state;
    }

    public boolean wasIn(S state) {
        return prev.get() == state;
    }

    public void transition(S state) {
        if (!getChoices().contains(state)) {
            throw new AssertionError("invalid transition");
        }

        batch(() -> {
            prev.accept(untrack(cur));
            cur.accept(state);
        });
    }
}
