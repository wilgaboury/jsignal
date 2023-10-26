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
        private final Signal<Boolean> showFire = createSignal(false);

        @Override
        public Nodes render() {
            return Nodes.component(new Scroller(Nodes.single(Node.builder()
                    .layout(Flex.builder()
                            .stretch()
                            .center()
                            .border(10f)
                            .column()
                            .gap(16f)
                            .padding(new Insets(25))
                            .build())
                    .paint(BasicPainter.builder()
                            .background(EzColors.AMBER_300)
                            .radius(50f)
                            .border(10f)
                            .borderColor(EzColors.EMERALD_500)
                            .build())
                    .children(Nodes.compose(
                            Nodes.single(Text.para(constantSupplier(
                                    Text.basicPara("Screw you guys I'm going home!", EzColors.CYAN_600, 25f)
                            ))),
                            Nodes.single(Text.line(constantSupplier(Text.basicTextLine("little longer text line", 14f)),
                                    constantSupplier(EzColors.FUCHSIA_800)
                            )),
                            Nodes.component(Button.builder()
                                    .setColor(color)
                                    .setText(this::buttonText)
                                    .setSize(Button.Size.LG)
                                    .setAction(() ->  {
                                        color.accept(random.nextInt());
                                        showFire.accept(show -> !show);
                                    })
                                    .build()
                            ),
                            Nodes.compute(() -> showFire.get()
                                    ? Nodes.single(Node.builder()
                                            .layout(Flex.builder()
                                                    .width(100f)
                                                    .height(50f)
                                                    .build()
                                            )
                                            .children(Nodes.single(
                                                    Image.builder()
                                                            .setFit(Image.Fit.FILL)
                                                            .setBlob(Blob.fromResource("/fire.svg", MediaType.SVG_UTF_8))
                                                            .build()
                                            ))
                                            .build())
                                    : Nodes.empty()
                            ),
                            Nodes.single(Node.builder()
                                    .layout(Flex.builder()
                                            .width(200f)
                                            .height(500f)
                                            .build()
                                    )
                                    .children(Nodes.single(
                                            Image.builder()
                                                    .setFit(Image.Fit.FILL)
                                                    .setBlob(Blob.fromResource("/cartman.svg", MediaType.SVG_UTF_8))
                                                    .build()
                                    ))
                                    .build())
                    ))
                    .build()
            )));
        }

        public String buttonText() {
            return showFire.get() ? "Hide Fire" : "Show Fire";
        }
    }
}
