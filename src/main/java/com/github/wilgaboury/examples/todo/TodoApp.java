package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.sigui.EventHandler;
import com.github.wilgaboury.sigui.Events;
import com.github.wilgaboury.sigui.SiguiThread;
import com.github.wilgaboury.sigui.Window;
import com.github.wilgaboury.sigwig.Center;
import com.github.wilgaboury.sigwig.Text;

public class TodoApp {
    public static void main(String[] args) {
        SiguiThread.getInstance().start();
        SiguiThread.invokeLater(() ->
                Window.create(
                        Events.register(EventHandler.onMouseClick(e -> System.out.println("clicked")),
                                Center.create(Text.create("Hi")))
                )
        );
    }
}
