package com.github.wilgaboury.sigwig

fun percent(v: Float) = MaybePercent(true, v)
fun pixel(v: Float) = MaybePercent(false, v)

@JvmRecord
data class MaybePercent<T>(val isPercent: Boolean, val value: T)
