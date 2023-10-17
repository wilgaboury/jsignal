package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.*;
import io.github.humbleui.jwm.Window;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class TodoApp {
    public static Context<Integer> TEST_CONTEXT = createContext(null);

    public static void main(String[] args) {
        Sigui.start(TodoApp::runApp);
    }

    public static void runApp() {
        Window window = Sigui.createWindow();
        window.setTitle("Todo List");

        Signal<Boolean> isBall = createSignal(false);

//        var siguiWindow = SiguiWindow.create(window,
//                () -> createProvider(TEST_CONTEXT.provide(0), () ->
//                        Box.builder()
//                                .events(() -> List.of(
//                                        EventListener.onMouseClick(e -> isBall.accept(v -> !v))
//                                ))
//                                .style(() -> Style.builder()
//                                        .background(isBall.get() ? EzColors.FUCHSIA_600 : EzColors.BLACK)
//                                        .radius(50f)
//                                        .border(15f)
//                                        .borderColor(isBall.get() ? EzColors.GRAY_600 : EzColors.RED_100)
//                                        .padding(new Insets(10, 10))
//                                        .column()
//                                        .center()
//                                        .build()
//                                )
//                                .children(createComputed(() -> ballList(5, isBall)))
//                )
//        );

//        Events.listen(siguiWindow.getRoot().getNode(), EventListener.onKeyDown(e -> {
//            System.out.println(e.getEvent().getKey().getName());
//        }));
    }

    public static List<Component> ballList(int num, Supplier<Boolean> isBall) {
        System.out.println(useContext(TEST_CONTEXT));
        return Stream.generate(() -> When.create(isBall, Circle::create, Rectangle::create))
                .limit(num)
                .toList();
    }

    public static class App extends Component {
        private final Signal<Boolean> isBall = createSignal(false);

        @Override
        public Nodes render() {
            return Nodes.single(Node.builder()
                    .setLayout(yoga -> {
                        // configure layout
                    })
                    .setPaint((canvas, yoga) -> {
                        // do painting
                    })
                    .setChildren(Nodes.fixed(
                            Node.builder()
                                    .setPaint((canvas, yoga) -> {
                                        // do painting
                                    })
                                    .setChildren(Nodes.none())
                                    .build()
                    ))
                    .build()
            );
        }
    }
}
