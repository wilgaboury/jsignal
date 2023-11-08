package com.github.wilgaboury.sigwig;

public record MaybePercent<T>(boolean isPercent, T value) {}
