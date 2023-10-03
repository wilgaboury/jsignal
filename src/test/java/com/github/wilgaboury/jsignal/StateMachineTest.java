package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.state.StateMachine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class StateMachineTest {
    private enum States {
        NEW,
        READY,
        RUNNING,
        WAITING,
        FINISHED
    }

    private static final Map<States, Collection<States>> transitions = Map.of(
            States.NEW, List.of(States.READY),
            States.READY, List.of(States.RUNNING),
            States.RUNNING, List.of(States.READY, States.WAITING, States.FINISHED),
            States.WAITING, List.of(States.READY),
            States.FINISHED, List.of()
    );

    @Test
    public void simpleStateMachineTest() {
        StateMachine<States> machine = new StateMachine<>(transitions, createSignal(States.NEW), createSignal(null));

        Ref<Boolean> atReady = new Ref<>(false);

        createEffect(() -> {
            switch (machine.getCur()) {
                case READY:
                    atReady.set(true);
                    break;
            }
        });

        machine.transition(States.READY);

        Assertions.assertTrue(atReady.get());
    }
}
