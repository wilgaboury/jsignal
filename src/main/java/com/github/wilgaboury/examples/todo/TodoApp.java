package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.ReactiveList;
import com.github.wilgaboury.sigui.Sigui;
import com.github.wilgaboury.sigui.SiguiWindow;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.Events;
import com.github.wilgaboury.sigwig.Circle;
import com.github.wilgaboury.sigwig.Flex;

public class TodoApp {
    public static void main(String[] args) {
        Sigui.start(() -> {
            var window = Sigui.createWindow();
            window.setTitle("Todo App");

            SiguiWindow.create(window,
                    () -> Events.listen(
                            EventListener.onMouseClick(e -> System.out.println("clicked")),
                            Flex.builder()
                                    .center()
                                    .row()
                                    .children(ReactiveList.of(
                                            Circle.create(),
                                            Circle.create(),
                                            Circle.create()
                                    ))
                    )
            );
        });
    }
}
