package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.*;
import com.google.common.net.MediaType;
import io.github.humbleui.jwm.Window;

import java.util.Random;

import static com.github.wilgaboury.jsignal.ReactiveUtil.constantSupplier;
import static com.github.wilgaboury.jsignal.ReactiveUtil.createSignal;

public class TodoApp {
    public static void main(String[] args) {
        Sigui.start(TodoApp::runApp);
    }

    public static void runApp() {
        Window window = Sigui.createWindow();
        window.setTitle("Test App");
        SiguiWindow.create(window, App::new);
    }

    public static class App extends Component {
        private final Random random = new Random();
        private final Signal<Integer> color = createSignal(random.nextInt());

        @Override
        public Nodes render() {
            return Nodes.single(Node.builder()
                    .setLayout(Flex.builder()
                            .stretch()
                            .center()
                            .border(10f)
                            .column()
                            .gap(16f)
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
                                    Text.basicPara("Screw you guys I'm really really really going home!", EzColors.CYAN_600, 25f)
                            ))),
                            Nodes.single(Text.line(constantSupplier(Text.basicTextLine("little longer text line", 14f)),
                                    constantSupplier(EzColors.FUCHSIA_800)
                            )),
                            Nodes.component(Button.builder()
                                    .setColor(color)
                                    .setText("My Button (changes color)!")
                                    .setSize(Button.Size.MD)
                                    .setAction(() -> color.accept(random.nextInt()))
                                    .build()
                            ),
                            Nodes.single(Node.builder()
                                    .setLayout(Flex.builder()
                                            .width(400f)
                                            .height(300f)
                                            .build()
                                    )
                                    .setChildren(Nodes.single(
                                            Image.builder()
                                                    .setFit(Image.Fit.COVER)
                                                    .setBlob(Blob.fromResource("/cartman.svg", MediaType.SVG_UTF_8))
                                                    .build()
                                    ))
                                    .build())
                    ))
                    .build()
            );
        }
    }
}
