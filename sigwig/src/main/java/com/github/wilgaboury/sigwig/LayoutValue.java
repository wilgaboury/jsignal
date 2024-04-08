package com.github.wilgaboury.sigwig;

public record LayoutValue(float value, Unit unit) {
    public enum Unit {
        PIXEL,
        PERCENT
    }

    public static LayoutValue pixel(float value) {
        return new LayoutValue(value, Unit.PIXEL);
    }

    public static LayoutValue percent(float value) {
        return new LayoutValue(value, Unit.PERCENT);
    }
}
