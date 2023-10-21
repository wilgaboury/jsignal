package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.Events;
import com.github.wilgaboury.sigwig.*;
import io.github.humbleui.jwm.Window;
import io.github.humbleui.skija.Font;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class TodoApp {
    public static Context<Integer> TEST_CONTEXT = createContext(null);

    public static void main(String[] args) {
        Sigui.start(TodoApp::runApp);
    }

    public static void runApp() {
        Window window = Sigui.createWindow();
        window.setTitle("Todo List");

        createProvider(MY_CONTEXT.provide(5), TodoApp::someFunction);

        var siguiWindow = SiguiWindow.create(window, App::new);

        Events.listen(siguiWindow.getRoot().getNode(), EventListener.onKeyDown(e -> {
            System.out.println(e.getEvent().getKey().getName());
        }));
    }

    public static Context<Integer> MY_CONTEXT = createContext(0);

    public static void someFunction() {
        var contextValue = useContext(MY_CONTEXT);
        System.out.println(contextValue);
    }

    public static class App extends Component {
        @Override
        public Nodes render() {
            return createProvider(TEST_CONTEXT.provide(0),
                    () -> Nodes.single(Node.builder()
                            .setLayout(Flex.builder()
                                    .center()
                                    .border(10f)
                                    .column()
                                    .gap(20f)
                                    .padding(new Insets(10, 10))
                                    .build())
                            .setPaint(BasicPainter.builder()
                                    .background(EzColors.AMBER_300)
                                    .radius(50f)
                                    .border(10f)
                                    .borderColor(EzColors.EMERALD_500)
                                    .build())
                            .setChildren(Nodes.compose(
                                    Nodes.single(Text.para(constantSupplier(
                                            Text.basicPara("Yo, Tim how is the movie?", EzColors.CYAN_600, 25f)
                                    ))),
                                    Nodes.single(Text.line(constantSupplier("the moon shit or somthing"),
                                            constantSupplier(EzColors.FUCHSIA_800),
                                            constantSupplier(run(() -> {
                                                Font font = new Font();
                                                font.setTypeface(Text.INTER_REGULAR);
                                                font.setSize(15f);
                                                return font;
                                            }))
                                    )),
                                    Nodes.component(Button.builder()
                                            .setColor(EzColors.FUCHSIA_500)
                                            .setText("My Button")
                                            .build()
                                    )
                            ))
                            .build()
            ));
        }
    }
}
