package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Sigui;
import com.github.wilgaboury.sigui.SiguiWindow;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigwig.*;
import io.github.humbleui.jwm.Window;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TodoApp {
    public static Context<Integer> TEST_CONTEXT = ReactiveUtil.createContext(null);

    public static void main(String[] args) {
        Sigui.setEnableHotSwap(true);
//        Sigui.setEnableHotRestart(true);
        Sigui.start(TodoApp::runApp);
    }

    public static void runApp() {
        Window window = Sigui.createWindow();
        window.setTitle("Todo List");

        Signal<Boolean> isBall = ReactiveUtil.createSignal(false);

        SiguiWindow.create(window,
                () -> ReactiveUtil.createProvider(TEST_CONTEXT.provide(0), () ->
                        Box.builder()
                                .events(() -> List.of(
                                        EventListener.onMouseClick(e -> isBall.accept(v -> !v))
                                ))
                                .style(() -> Style.builder()
                                        .background(isBall.get() ? EzColors.FUCHSIA_600 : EzColors.BLACK)
                                        .border(new Insets(15))
                                        .borderColor(isBall.get() ? EzColors.SLATE_500 : EzColors.RED_100)
                                        .padding(new Insets(10, 10))
                                        .column()
                                        .center()
                                        .build()
                                )
                                .children(ReactiveUtil.createComputed(() -> ballList(5, isBall)))
                )
        );
    }

    public static List<Component> ballList(int num, Supplier<Boolean> isBall) {
        System.out.println(ReactiveUtil.useContext(TEST_CONTEXT));
        return Stream.generate(() -> When.create(isBall, Circle::create, Rectangle::create))
                .limit(num)
                .toList();
    }
}
