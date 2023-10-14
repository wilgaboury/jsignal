package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.NodeDecorator;
import com.github.wilgaboury.sigui.Sigui;
import com.github.wilgaboury.sigui.event.EventListener;
import com.github.wilgaboury.sigui.event.Events;
import io.github.humbleui.skija.Canvas;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class Box {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Supplier<Style> style = () -> Style.builder().build();
        private Supplier<List<EventListener>> listeners = Collections::emptyList;
        private boolean focus = false;

        public Builder style(Supplier<Style> style) {
            this.style = style;
            return this;
        }

        public Builder events(Supplier<List<EventListener>> listeners) {
            this.listeners = listeners;
            return this;
        }

        public Builder focus() {
            focus = true;
            return this;
        }

        public Component apply(Supplier<Node> inner) {
            return Component.create(applyRaw(inner));
        }

        public Supplier<Node> applyRaw(Supplier<Node> inner) {
            return () -> applyListeners(listeners, new NodeDecorator(inner.get()) {
                @Override
                public boolean focus() {
                    return focus;
                }

                @Override
                public void layout(long node) {
                    style.get().layout(node);
                }

                @Override
                public void paint(Canvas canvas, long yoga) {
                    style.get().paint(canvas, yoga);
                }
            });
        }

        public Component child(Component child) {
            return Component.create(childRaw(child));
        }

        public Supplier<Node> childRaw(Component child) {
            return () -> applyListeners(listeners, new Node() {
                @Override
                public List<Component> children() {
                    return List.of(child);
                }

                @Override
                public boolean focus() {
                    return focus;
                }

                @Override
                public void layout(long node) {
                    style.get().layout(node);
                }

                @Override
                public void paint(Canvas canvas, long yoga) {
                    style.get().paint(canvas, yoga);
                }
            });
        }

        public Component children(List<Component> children) {
            return Component.create(childrenRaw(children));
        }

        public Supplier<Node> childrenRaw(List<Component> children) {
            return () -> applyListeners(listeners, new Node() {
                @Override
                public List<Component> children() {
                    return children;
                }

                @Override
                public boolean focus() {
                    return focus;
                }

                @Override
                public void layout(long node) {
                    style.get().layout(node);
                }

                @Override
                public void paint(Canvas canvas, long yoga) {
                    style.get().paint(canvas, yoga);
                }
            });
        }

        public Component children(Computed<List<Component>> children) {
            return Component.create(childrenRaw(children));
        }

        public Supplier<Node> childrenRaw(Computed<List<Component>> children) {
            return () -> applyListeners(listeners, new Node() {
                @Override
                public List<Component> children() {
                    return children.get();
                }

                @Override
                public boolean focus() {
                    return focus;
                }

                @Override
                public void layout(long node) {
                    style.get().layout(node);
                }

                @Override
                public void paint(Canvas canvas, long yoga) {
                    style.get().paint(canvas, yoga);
                }
            });
        }
    }

    public static Node applyListeners(Supplier<List<EventListener>> listeners, Node node) {
        createEffect(() -> {
            Sigui.hotSwapTrigger.track();

            var list = listeners.get();
            onCleanup(() -> {
                list.forEach(listener -> Events.unlisten(node, listener));
            });
            list.forEach(listener -> Events.listen(node, listener));
        });

        return node;
    }
}
