package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.Events;
import com.github.wilgaboury.sigwig.*;
import io.github.humbleui.jwm.Window;

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

        var siguiWindow = SiguiWindow.create(window, App::new);

        Events.listen(siguiWindow.getRoot().getNode(), EventListener.onKeyDown(e -> {
            System.out.println(e.getEvent().getKey().getName());
        }));
    }

    public static class App extends Component {
        private final Signal<Boolean> isBall = createSignal(false);

        @Override
        public Nodes render() {
            return createProvider(TEST_CONTEXT.provide(0),
                    () -> Nodes.single(Node.builder()
                            .listen(EventListener.onMouseClick(e -> isBall.accept(v -> !v)))
                            .setLayout(Flex.builder()
                                    .center()
                                    .border(4f)
                                    .row()
                                    .wrap()
                                    .padding(new Insets(10, 10))
                                    .build())
                            .setPaint(BasicPainter.builder()
                                    .background(EzColors.AMBER_300)
                                    .radius(10f)
                                    .border(4f)
                                    .borderColor(EzColors.EMERALD_500)
                                    .build())
                            .setChildren(Nodes.compose(
                                    Stream.generate(() -> isBall.get() ? new Circle(25f) : new Circle(50f))
                                            .map(Nodes::component)
                                            .limit(100)
                                            .toList()
                            ))
                            .build()
            ));
        }
    }
}
