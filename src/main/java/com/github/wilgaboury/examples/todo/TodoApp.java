package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.sigui.Sigui;
import com.github.wilgaboury.sigui.SiguiWindow;
import com.github.wilgaboury.sigwig.Center;
import com.github.wilgaboury.sigwig.Circle;

public class TodoApp {
    public static void main(String[] args) {
        Sigui.start(() ->
                SiguiWindow.create(
                        () -> Center.create(Circle.create())
//                        Events.register(EventHandler.onMouseClick(e -> System.out.println("clicked")),
//                                Center.create(Text.create("Hi")))
                )
        );
    }
}
