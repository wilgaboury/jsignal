package todo;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.*;
import com.google.common.net.MediaType;
import io.github.humbleui.jwm.Window;
import io.github.humbleui.skija.Color;

import java.util.Random;

import static com.github.wilgaboury.jsignal.ReactiveUtil.constantSupplier;
import static com.github.wilgaboury.jsignal.ReactiveUtil.createSignal;

public class TodoApp {
    private static final String LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin porttitor erat nec mi cursus semper. Nam dignissim auctor aliquam. Morbi eu arcu tempus, ullamcorper libero ut, faucibus erat. Mauris vel nisl porta, finibus quam nec, blandit lacus. In bibendum ligula porta dolor vehicula blandit tempus finibus orci. Phasellus pulvinar eros eu ipsum aliquam interdum. Curabitur ac arcu feugiat, pellentesque est non, aliquam dolor. Curabitur vel ultrices mi. Nullam eleifend nec tellus a viverra. Sed congue lacus at est maximus, vel elementum libero rhoncus. Donec at fermentum lectus. Vestibulum sodales augue in risus dapibus blandit.";

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
        private final Signal<Integer> color = createSignal(Color.withA(EzColors.BLACK, 255));//random.nextInt(), 255));
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
                            Nodes.single(Text.para(Text.basicPara(LOREM, EzColors.BLACK, 18f))),
                            Nodes.single(Text.line(
                                    constantSupplier(Text.basicTextLine("small text line", 14f)),
                                    constantSupplier(EzColors.FUCHSIA_800)
                            )),
                            Nodes.component(Button.builder()
                                    .setColor(color)
                                    .setText(this::buttonText)
                                    .setSize(Button.Size.LG)
                                    .setAction(() ->  {
                                        color.accept(Color.withA(random.nextInt(), 255));
                                        showFire.accept(show -> !show);
                                    })
                                    .build()
                            ),
                            Nodes.compute(() -> showFire.get()
                                    ? Nodes.single(Image.builder()
                                            .fit(Image.Fit.FILL)
                                            .height(200f)
                                            .widthPercent(100f)
                                            .blob(Blob.fromResource("/fire.svg", MediaType.SVG_UTF_8))
                                            .build()
                                    )
                                    : Nodes.empty()
                            ),
                            Nodes.single(Image.builder()
                                    .width(250f)
                                    .blob(Blob.fromResource("/peng.png", MediaType.PNG))
                                    .build()
                            )
                    ))
                    .build()
            )));
        }

        private String buttonText() {
            return (showFire.get() ? "Hide Fire" : "Show Fire") + " (and changes color)";
        }
    }
}
