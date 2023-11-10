package com.github.wilgaboury.ktsigui

import com.github.wilgaboury.sigui.Sigui
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.internal.MainDispatcherFactory
import kotlin.coroutines.CoroutineContext

// TODO: implement delay like kotlinx swing dispatcher

internal object SiguiDispatcher : MainCoroutineDispatcher() {
    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun dispatch(context: CoroutineContext, block: Runnable): Unit = Sigui.invokeLater(block)
}

@Suppress("unused")
@OptIn(InternalCoroutinesApi::class)
internal class SiguiMainDispatcherFactory : MainDispatcherFactory {
    override val loadPriority: Int
        get() = 0

    override fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher = SiguiDispatcher
}