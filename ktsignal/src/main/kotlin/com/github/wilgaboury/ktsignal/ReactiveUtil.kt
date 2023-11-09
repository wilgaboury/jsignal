package com.github.wilgaboury.ktsignal

import com.github.wilgaboury.jsignal.AtomicSignal
import com.github.wilgaboury.jsignal.Signal
import java.util.*

fun <T> createSignal(
        value: T,
        equals: (T, T) -> Boolean = Objects::deepEquals,
        clone: (T) -> T = { it },
        async: Boolean = false,
): Signal<T> = Signal(value, equals, clone, !async)

fun <T> createAtomicSignal(
        value: T,
        equals: (T, T) -> Boolean = Objects::deepEquals,
        clone: (T) -> T = { it }
): Signal<T> = AtomicSignal(value, equals, clone)