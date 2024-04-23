package com.github.wilgaboury.sigui.layout;

public sealed interface LayoutValue {
    float value();

    record Pixel(float value) implements LayoutValue {}
    record Percent(float value) implements LayoutValue {}

    static Pixel pixel(float value) {
        return new Pixel(value);
    }

    static Percent percent(float value) {
        return new Percent(value);
    }
}
