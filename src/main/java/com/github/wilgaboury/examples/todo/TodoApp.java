package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.sigui.Sigui;
import com.github.wilgaboury.sigui.SiguiWindow;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.Events;
import com.github.wilgaboury.sigwig.Circle;
import com.github.wilgaboury.sigwig.Flex;

import java.util.stream.Stream;

public class TodoApp {
    public static void main(String[] args) {
        Sigui.start(() -> {
            var window = Sigui.createWindow();
            window.setTitle("Todo App");

            var num = ReactiveUtil.createSignal(0);

            SiguiWindow.create(window,
                    () -> Events.listen(
                            EventListener.onMouseClick(e -> num.accept(i -> i + 1)),
                            Flex.builder()
                                    .center()
                                    .row()
                                    .children(() ->
                                            Stream.generate(Circle::create)
                                                    .limit(num.get())
                                                    .toList()
                                    )
                    )
            );
        });
    }
}
