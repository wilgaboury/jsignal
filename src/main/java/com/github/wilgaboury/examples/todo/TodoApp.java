package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.Context;
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
import java.util.stream.Stream;

public class TodoApp {
    public static Context<Integer> TEST_CONTEXT = ReactiveUtil.createContext(null);

    public static void main(String[] args) {
//        Sigui.setEnableHotSwap(true);
        Sigui.setEnableHotRestart(true);
        Sigui.start(TodoApp::runApp);
    }

    public static void runApp() {
        Window window = Sigui.createWindow();
        window.setTitle("Todo List");

        Signal<Integer> num = ReactiveUtil.createSignal(2);

        SiguiWindow.create(window,
                () -> ReactiveUtil.createProvider(TEST_CONTEXT.provide(0), () -> Events.listen(
                        EventListener.onMouseClick(e -> num.accept(i -> i + 1)),
                        Box.create(() -> Style.builder().center().column().build(), ReactiveUtil.createComputed(() -> ballList(num.get())))
                ))
        );
    }

    public static List<Component> ballList(int num) {
        System.out.println(ReactiveUtil.useContext(TEST_CONTEXT));
        Sigui.hotSwapTrigger.track();
        return Stream.generate(() ->
                        Events.listen(List.of(
                                        EventListener.onMouseOut(e -> System.out.println("mouse out circle")),
                                        EventListener.onMouseLeave(e -> System.out.println("mouse leave circle"))
                                ),
                                Circle.create()
                        ))
                .limit(num)
                .toList();
    }
}
