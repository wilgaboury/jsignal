package com.github.wilgaboury.ksigui

import com.github.wilgaboury.jsignal.ReactiveUtil
import com.github.wilgaboury.sigui.SiguiExecutor
import com.github.wilgaboury.sigui.SiguiUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.internal.MainDispatcherFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

@OptIn(InternalCoroutinesApi::class)
internal object SiguiDispatcher : MainCoroutineDispatcher(), Delay {
    private val schedule = Executors.newSingleThreadScheduledExecutor()

    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = !SiguiUtil.onThread()

    override fun dispatch(context: CoroutineContext, block: Runnable): Unit = SiguiExecutor.invokeLater(block)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val fut = schedule.schedule({
            with(continuation) {
                resumeUndispatched(Unit)
            }
        }, timeMillis, TimeUnit.MILLISECONDS)
        continuation.invokeOnCancellation { fut.cancel(false) }
    }

    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle {
        val fut = schedule.schedule(block, timeMillis, TimeUnit.MILLISECONDS)
        return DisposableHandle { fut.cancel(false) }
    }
}

@Suppress("unused")
@OptIn(InternalCoroutinesApi::class)
internal class SiguiMainDispatcherFactory : MainDispatcherFactory {
    override val loadPriority: Int
        get() = 0

    override fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher = SiguiDispatcher
}