package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.sigui.SiguiThread;
import com.github.wilgaboury.sigui.Window;
import com.github.wilgaboury.sigwig.Center;
import com.github.wilgaboury.sigwig.Text;

public class TodoApp {
    public static void main(String[] args) {
        SiguiThread.getInstance().start();
        SiguiThread.invokeLater(() -> Window.create(Center.create(Text.create("Hi"))));
    }
}
