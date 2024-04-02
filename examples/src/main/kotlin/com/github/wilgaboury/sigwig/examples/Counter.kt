package com.github.wilgaboury.sigwig.examples

import com.github.wilgaboury.ksignal.createSignal
import com.github.wilgaboury.ksigui.flex
import com.github.wilgaboury.ksigui.node
import com.github.wilgaboury.sigui.Component
import com.github.wilgaboury.sigui.Nodes
import com.github.wilgaboury.sigui.SiguiUtil
import com.github.wilgaboury.sigui.SiguiWindow
import com.github.wilgaboury.sigwig.Button
import com.github.wilgaboury.sigwig.EzColors
import com.github.wilgaboury.sigwig.Line

fun main() {
    SiguiUtil.start {
        val window = SiguiUtil.createWindow()
        window.setTitle("Counter")
        window.setContentSize(400, 400)
        SiguiWindow(window) { Counter() }
    }
}

class Counter : Component() {
    private val count = createSignal(0)

    override fun render(): Nodes {
        return node {
            layout(flex {
                stretch()
                center()
                column()
                gap(10f)
            })
            children(
                Line({ Line.basic("Count: ${count.get()}", 20f) }, { EzColors.BLUE_500 }),
                Button(
                    color = { EzColors.BLUE_300 },
                    text = { "Increment" },
                    action = { count.accept { c -> c + 1 } }
                )
            )
        }
    }
}