package com.github.wilgaboury.sigwig.examples

import com.github.wilgaboury.ksignal.createSignal
import com.github.wilgaboury.ksignal.supply
import com.github.wilgaboury.ksigui.flex
import com.github.wilgaboury.ksigui.node
import com.github.wilgaboury.sigui.*
import com.github.wilgaboury.sigui.Nodes.*
import com.github.wilgaboury.sigwig.*
import com.google.common.net.MediaType
import io.github.humbleui.skija.Color
import java.util.*

const val LOREM =
    "Lorem ipsum dolor sit amet, consec tetur adipiscing elit. Proin porttitor erat nec mi cursus semper. Nam dignissim auctor aliquam. Morbi eu arcu tempus, ullamcorper libero ut, faucibus erat. Mauris vel nisl porta, finibus quam nec, blandit lacus. In bibendum ligula porta dolor vehicula blandit tempus finibus orci. Phasellus pulvinar eros eu ipsum aliquam interdum. Curabitur ac arcu feugiat, pellentesque est non, aliquam dolor. Curabitur vel ultrices mi. Nullam eleifend nec tellus a viverra. Sed congue lacus at est maximus, vel elementum libero rhoncus. Donec at fermentum lectus. Vestibulum sodales augue in risus dapibus blandit."

fun main() {
    SiguiUtil.start {
        val window = SiguiUtil.createWindow()
        window.setTitle("Test App")
        window.setContentSize(400, 400)
        SiguiWindow(window) { App() }
    }
}

class App : Component() {
    private val random = Random()

    private val buttonColor = createSignal(Color.withA(EzColors.BLACK, 255))
    private val showFire = createSignal(false)

    private val count = createSignal(0);

    override fun render(): Nodes {
        return Scroller(barWidth = { 15f }) {
            node {
                layout(flex {
                    stretch()
                    center()
                    border(10f)
                    column()
                    gap(16f)
                    padding(Insets(25f))
                    widthPercent(100f)
                })
                paint(
                    BasicPainter(background = { EzColors.AMBER_300 },
                        radius = { 50f },
                        border = { 10f },
                        borderColor = { EzColors.EMERALD_500 })
                )
                children(
                    compose(
                        Button(color = { EzColors.BLUE_300 }, text = { "Count: ${count.get()}" }, action = { count.accept { c -> c + 5 } }).render(),
                        Para(Para.basic(LOREM, EzColors.BLACK, 12f)).render(),
                        Para(Para.basic(LOREM, EzColors.BLACK, 10f)).render(),
                        Para(Para.basic(LOREM, EzColors.BLACK, 8f)).render(),
                        Line(supply { Line.basic("change text line", 20f) }, { EzColors.FUCHSIA_800 }).render(),
                        Button(
                            color = { buttonColor.get() },
                            text = this@App::buttonText,
                            size = { Button.Size.LG },
                            action = {
                                buttonColor.accept(Color.withA(random.nextInt(), 255))
                                showFire.accept { show -> !show }
                            }).render(),
                        maybeFireImage(),
                        Image(supply { Blob.fromResource("/peng.png", MediaType.PNG) },
                            fit = { Image.Fit.COVER },
                            width = supply { pixel(300f) },
                            height = supply { pixel(200f) }).render(),
                    )
                )
            }
        }.render()
    }

    private fun maybeFireImage(): Nodes {
        return compute {
            if (showFire.get()) {
                Image(supply { Blob.fromResource("/fire.svg", MediaType.SVG_UTF_8) },
                    fit = { Image.Fit.CONTAIN },
                    width = supply { percent(100f) },
                    height = supply { pixel(200f) }).render()
            } else {
                empty()
            }
        }
    }

    private fun buttonText(): String {
        return (if (showFire.get()) "Hide Fire" else "Show Fire") + " (and changes color)"
    }
}
