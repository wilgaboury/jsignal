package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.ReactiveList;
import com.github.wilgaboury.sigui.Sigui;
import com.github.wilgaboury.sigui.SiguiWindow;
import com.github.wilgaboury.sigwig.Center;
import com.github.wilgaboury.sigwig.Circle;
import com.github.wilgaboury.sigwig.Column;
import com.github.wilgaboury.sigwig.Row;

public class TodoApp {
    public static void main(String[] args) {
        Sigui.start(() ->
                SiguiWindow.create(
                        () -> Center.create(
                                Row.create(ReactiveList.of(
                                        Circle.create(),
                                        Circle.create(),
                                        Circle.create()
                                ))
                        )
//                        Events.register(EventHandler.onMouseClick(e -> System.out.println("clicked")),
//                                Center.create(Text.create("Hi")))
                )
        );
    }
}
