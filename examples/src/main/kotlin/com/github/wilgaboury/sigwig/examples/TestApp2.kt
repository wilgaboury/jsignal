package com.github.wilgaboury.sigwig.examples

import com.github.wilgaboury.jsignal.ReactiveUtil
import com.github.wilgaboury.jsignal.Ref
import com.github.wilgaboury.ksigui.flex
import com.github.wilgaboury.ksigui.node
import com.github.wilgaboury.ksigui.ref
import com.github.wilgaboury.sigui.Component
import com.github.wilgaboury.sigui.MetaNode
import com.github.wilgaboury.sigui.Nodes
import com.github.wilgaboury.sigui.SiguiUtil
import com.github.wilgaboury.sigui.SiguiWindow
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

    override fun render(): Nodes {
        onMount {
            ReactiveUtil.createEffect {
                println(reference.get())
            }
        }

        return node {
            ref {
                reference.set(this)
            }
            layout(flex {
                row()
                gap(10f)
            })
            children(
                Circle(radius = { 10f }),
                Circle(radius = { 20f }),
//                Circle(radius = { 30f }),
//                Circle(radius = { 40f }),
            )
        };
    }
}