package todo.com.github.wilgaboury.sigwig.examples

import com.github.wilgaboury.jsignal.ReactiveUtil.createSignal
import com.github.wilgaboury.jsignal.interfaces.SignalLike
import com.github.wilgaboury.ksignal.supply
import com.github.wilgaboury.ksigui.flex
import com.github.wilgaboury.ksigui.node
import com.github.wilgaboury.ksigui.toNodes
import com.github.wilgaboury.sigui.*
import com.github.wilgaboury.sigwig.*
import com.google.common.net.MediaType
import io.github.humbleui.skija.Color
import java.util.*

const val LOREM = "Lorem ipsum dolor sit amet, consec tetur adipiscing elit. Proin porttitor erat nec mi cursus semper. Nam dignissim auctor aliquam. Morbi eu arcu tempus, ullamcorper libero ut, faucibus erat. Mauris vel nisl porta, finibus quam nec, blandit lacus. In bibendum ligula porta dolor vehicula blandit tempus finibus orci. Phasellus pulvinar eros eu ipsum aliquam interdum. Curabitur ac arcu feugiat, pellentesque est non, aliquam dolor. Curabitur vel ultrices mi. Nullam eleifend nec tellus a viverra. Sed congue lacus at est maximus, vel elementum libero rhoncus. Donec at fermentum lectus. Vestibulum sodales augue in risus dapibus blandit."

fun main(args: Array<String>) {
    SiguiUtil.start {
        val window = SiguiUtil.createWindow()
        window.setTitle("Test App")
        window.setContentSize(400, 400)
        SiguiWindow.create(window) { App() }
    }
}

class App : Component() {
    private val random = Random()
    private val color: SignalLike<Int> = createSignal(Color.withA(EzColors.BLACK, 255)) //random.nextInt(), 255));
    private val show: SignalLike<Boolean> = createSignal(false)

    override fun render(): Nodes {
        return Scroller(node {
            layout(flex {
                stretch()
                center()
                border(10f)
                column()
                gap(16f)
                padding(Insets(25f))
            })
            paint(BasicPainter(
                    background = { EzColors.AMBER_300 },
                    radius = { 50f },
                    border = { 10f },
                    borderColor = { EzColors.EMERALD_500 }
            ))
            children(Nodes.compose(
                    Text.para(supply { Text.basicPara(LOREM, EzColors.BLACK, 12f) }),
                    Text.line(
                            supply { Text.basicTextLine("change text line", 20f) },
                            { EzColors.FUCHSIA_800 }
                    ),
                    Button(
                            color = { color.get() },
                            text = this@App::buttonText,
                            size = { Button.Size.LG },
                            action = {
                                color.accept(Color.withA(random.nextInt(), 255))
                                show.accept { show -> !show }
                            }
                    ).toNodes(),
                    maybeFireImage(),
                    Image.create(
                            supply { Blob.fromResource("/peng.png", MediaType.PNG) },
                            fit = { Image.Fit.COVER },
                            width = supply { pixel( 100f ) },
                            height = supply { pixel( 200f ) }
                    ).toNodes()
            ))
        }).toNodes()
    }

    private fun maybeFireImage(): Nodes {
        return Nodes.compute {
            if (show.get()) {
                Nodes.single(Image.create(
                        supply { Blob.fromResource("/fire.svg", MediaType.SVG_UTF_8) },
                        fit = { Image.Fit.CONTAIN },
                        width = supply { percent( 100f ) },
                        height = supply { pixel( 200f ) }
                ))
            } else {
                Nodes.empty()
            }
        }
    }

    private fun buttonText(): String {
        return (if (show.get()) "Hide Fire" else "Show Fire") + " (and changes color)"
    }
}
