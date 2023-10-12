package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Sigui;
import com.github.wilgaboury.sigui.SiguiWindow;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.Events;
import com.github.wilgaboury.sigwig.Box;
import com.github.wilgaboury.sigwig.Circle;
import com.github.wilgaboury.sigwig.Style;
import io.github.humbleui.jwm.Window;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TodoApp2 {
    public static void main(String[] args) {
        Sigui.start(TodoApp2::runApp);
    }

    public static void runApp() {
        Window window = Sigui.createWindow();
        window.setTitle("Todo List");

        Signal<Integer> num = ReactiveUtil.createSignal(2);

        SiguiWindow.create(window,
                () -> Events.listen(
                        EventListener.onMouseClick(e -> num.accept(i -> i + 1)),
                        Box.create(() -> Style.builder().row().center().build(), () -> ballList(num))
                )
        );
    }

    public static List<Component> ballList(Supplier<Integer> num) {
        return Stream.generate(() ->
                        Events.listen(List.of(
                                        EventListener.onMouseOut(e -> System.out.println("mouse out circle")),
                                        EventListener.onMouseLeave(e -> System.out.println("mouse leave circle"))
                                ),
                                Circle.create()
                        ))
                .limit(num.get())
                .toList();
    }
}
