package com.github.wilgaboury.sigwig.examples

import com.github.wilgaboury.jsignal.ReactiveUtil
import com.github.wilgaboury.jsignal.Ref
import com.github.wilgaboury.ksignal.createSignal
import com.github.wilgaboury.ksigui.flex
import com.github.wilgaboury.ksigui.node
import com.github.wilgaboury.sigui.*
import com.github.wilgaboury.sigwig.Circle

fun main() {
    SiguiUtil.start {
        val window = SiguiUtil.createWindow()
        window.setTitle("Test App")
        window.setContentSize(400, 400)
        SiguiWindow(window) { App2() }
    }
}

class App2 : Component() {
    private val reference: Ref<MetaNode> = Ref();

    private val count = createSignal(0);

    override fun render(): Nodes {
        onMount {
//            println("test")
            ReactiveUtil.createEffect {
                println(reference.get())
            }
        }

        return node {
            layout(flex {
                column()
                gap(20f)
                padding(Insets(20f))
            })
            children(
//                node {
//                    ref {
//                        reference.set(this)
//                    }
//                    layout(flex {
//                        row()
//                        gap(20f)
//                    })
//                    children(
                        Circle(radius = { 10f }),
                        Circle(radius = { 20f }),
                        Circle(radius = { 30f }),
                        Circle(radius = { 40f }),
//                    )
//                },
//                Button( color = { EzColors.AMBER_800 }, text = { "${count}" }, action = { count.accept(5) } )
            )
        }
    }
}