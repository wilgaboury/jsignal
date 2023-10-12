package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import io.github.humbleui.skija.Canvas;

import java.util.List;
import java.util.function.Supplier;

public class Box {
    public static Component create(Supplier<Style> style, Component child) {
        return () -> new Node() {
            @Override
            public List<Component> children() {
                return List.of(child);
            }

            @Override
            public void layout(long node) {
                style.get().layout(node);
            }

            @Override
            public void paint(Canvas canvas) {
                style.get().paint(canvas);
            }
        };
    }

    public static Component create(Supplier<Style> style, List<Component> children) {
        return () -> new Node() {
            @Override
            public List<Component> children() {
                return children;
            }

            @Override
            public void layout(long node) {
                style.get().layout(node);
            }

            @Override
            public void paint(Canvas canvas) {
                style.get().paint(canvas);
            }
        };
    }

    public static Component create(Supplier<Style> style, Computed<List<Component>> children) {
        return () -> new Node() {
            @Override
            public List<Component> children() {
                return children.get();
            }

            @Override
            public void layout(long node) {
                style.get().layout(node);
            }

            @Override
            public void paint(Canvas canvas) {
                style.get().paint(canvas);
            }
        };
    }
}
