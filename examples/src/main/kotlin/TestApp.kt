package todo

import com.github.wilgaboury.jsignal.ReactiveUtil
import com.github.wilgaboury.jsignal.interfaces.SignalLike
import com.github.wilgaboury.sigui.*
import com.github.wilgaboury.sigwig.*
import com.google.common.net.MediaType
import io.github.humbleui.skija.Color
import java.util.*

object TodoApp {
    private const val LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin porttitor erat nec mi cursus semper. Nam dignissim auctor aliquam. Morbi eu arcu tempus, ullamcorper libero ut, faucibus erat. Mauris vel nisl porta, finibus quam nec, blandit lacus. In bibendum ligula porta dolor vehicula blandit tempus finibus orci. Phasellus pulvinar eros eu ipsum aliquam interdum. Curabitur ac arcu feugiat, pellentesque est non, aliquam dolor. Curabitur vel ultrices mi. Nullam eleifend nec tellus a viverra. Sed congue lacus at est maximus, vel elementum libero rhoncus. Donec at fermentum lectus. Vestibulum sodales augue in risus dapibus blandit."
    @JvmStatic
    fun main(args: Array<String>) {
        Sigui.start { runApp() }
    }

    fun runApp() {
        val window = Sigui.createWindow()
        window.setTitle("Test App")
        SiguiWindow.create(window) { App() }
    }

    class App : Component() {
        private val random = Random()
        private val color: SignalLike<Int> = ReactiveUtil.createSignal(Color.withA(EzColors.BLACK, 255)) //random.nextInt(), 255));
        private val show: SignalLike<Boolean> = ReactiveUtil.createSignal(false)
        override fun render(): Nodes {
            return Nodes.component(Scroller(Nodes.single(Node.builder()
                    .layout(Flex.builder()
                            .stretch()
                            .center()
                            .border(10f)
                            .column()
                            .gap(16f)
                            .padding(Insets(25f))
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
                                    { Text.basicTextLine("change text line", 14f) },
                                    { EzColors.FUCHSIA_800 }
                            )),
                            Nodes.component(Button(
                                    color = { color.get() },
                                    text = { buttonText() },
                                    size = { Button.Size.LG },
                                    action = {
                                        color.accept(Color.withA(random.nextInt(), 255))
                                        show.accept { show -> !show }
                                    }
                            )),
                            Nodes.compute {
                                if (show.get()) Nodes.single(Image.builder()
                                        .fit(Image.Fit.FILL)
                                        .height(200f)
                                        .widthPercent(100f)
                                        .blob(Blob.fromResource("/fire.svg", MediaType.SVG_UTF_8))
                                        .build()
                                ) else Nodes.empty()
                            },
                            Nodes.single(Image.builder()
                                    .fit(Image.Fit.COVER)
                                    .height(200f)
                                    .width(100f)
                                    .blob(Blob.fromResource("/peng.png", MediaType.PNG))
                                    .build()
                            )
                    ))
                    .build()
            )))
        }

        private fun buttonText(): String {
            return (if (show.get()) "Hide Fire" else "Show Fire") + " (and changes color)"
        }
    }
}
