package com.github.wilgaboury.ksigui

import com.github.wilgaboury.sigui.*
import com.github.wilgaboury.sigui.event.EventListener
import com.github.wilgaboury.sigui.event.KeyboardEvent
import com.github.wilgaboury.sigui.event.MouseEvent
import com.github.wilgaboury.sigui.event.ScrollEvent

fun node(extension: Node.Builder.() -> Unit): Nodes.Static {
    val builder = Node.builder()
    builder.extension()
    return builder.build().toNodes()
}

fun Node.toNodes(): Nodes.Static = Nodes.single(this)

fun Component.toNodes(): Nodes = Nodes.component(this)

class EventListenerBuilder(val node: MetaNode) {
    fun onMouseIn(listener: (MouseEvent) -> Unit) {
        node.listen(EventListener.onMouseIn(listener))
    }

    fun onMouseOut(listener: (MouseEvent) -> Unit) {
        node.listen(EventListener.onMouseOut(listener))
    }

    fun onMouseLeave(listener: (MouseEvent) -> Unit) {
        node.listen(EventListener.onMouseLeave(listener))
    }

    fun onMouseOver(listener: (MouseEvent) -> Unit) {
        node.listen(EventListener.onMouseOver(listener))
    }

    fun onMouseDown(listener: (MouseEvent) -> Unit) {
        node.listen(EventListener.onMouseDown(listener))
    }

    fun onMouseUp(listener: (MouseEvent) -> Unit) {
        node.listen(EventListener.onMouseUp(listener))
    }

    fun onMouseClick(listener: (MouseEvent) -> Unit) {
        node.listen(EventListener.onMouseClick(listener))
    }

    fun onKeyDown(listener: (KeyboardEvent) -> Unit) {
        node.listen(EventListener.onKeyDown(listener))
    }

    fun onScroll(listener: (ScrollEvent) -> Unit) {
        node.listen(EventListener.onScroll(listener))
    }
}

fun MetaNode.listen(extension: EventListenerBuilder.() -> Unit) {
    EventListenerBuilder(this).extension()
}

fun flex(extension: Flex.Builder.() -> Unit): Flex {
    val builder = Flex.builder()
    builder.extension()
    return builder.build()
}