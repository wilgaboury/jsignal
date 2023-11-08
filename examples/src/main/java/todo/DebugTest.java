package todo;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.*;
import com.google.common.net.MediaType;
import io.github.humbleui.jwm.Window;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createSignal;

public class DebugTest {
    public static void main(String[] args) {
        Sigui.start(DebugTest::runApp);
    }

    public static void runApp() {
        Window window = Sigui.createWindow();
        SiguiWindow.create(window, App::new);
    }

    public static class App extends Component {
        private final Signal<Boolean> show = createSignal(true);

        @Override
        public Nodes render() {
            return Nodes.component(new Scroller(Nodes.single(Node.builder()
                    .layout(Flex.builder()
                            .stretch()
                            .center()
                            .column()
                            .gap(16f)
                            .build())
                    .paint(BasicPainter.builder()
                            .background(EzColors.RED_200)
                            .build())
                    .children(Nodes.compose(
                            Nodes.component(Button.builder()
                                    .setColor(EzColors.AMBER_300)
                                    .setText("Button")
                                    .setSize(Button.Size.LG)
                                    .setAction(() ->  show.accept(s -> !s))
                                    .build()
                            ),
                            Nodes.compute(() -> show.get()
                                            ? Nodes.single(Image.builder()
                                            .fit(Image.Fit.FILL)
                                            .height(200f)
                                            .widthPercent(100f)
                                            .blob(Blob.fromResource("/fire.svg", MediaType.SVG_UTF_8))
                                            .build()
                                    )
                                            : Nodes.empty()
                            )
                    ))
                    .build()
            )));
        }
    }
}
