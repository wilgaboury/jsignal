package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.event.EventListener;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import java.util.Optional;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;
import static com.github.wilgaboury.sigui.event.EventListener.*;

public class Scroller extends Component {
    private final Nodes children;

    private final Signal<Float> yOffset;
    private final Signal<Boolean> mouseDown;
    private float yMouseDownOffset;
    private final Signal<Boolean> mouseOver;
    private final Ref<MetaNode> inner;
    private final Signal<Float> yScale;

    public Scroller(Nodes children) {
        this.children = children;
        this.yOffset = createSignal(0f);
        this.mouseOver = createSignal(false);
        this.mouseDown = createSignal(false);
        this.inner = new Ref<>();
        this.yScale = createSignal(0f);
    }

    @Override
    public Nodes render() {
        var window = SiguiWindow.useWindow();

        Ref<MetaNode> outer = new Ref<>();

        onMount(() -> {
            createEffect(() -> {
                if (mouseDown.get()) {
                    createEffect(onDefer(window::getMousePosition, (pos) -> {
                        var rel = MathUtil.apply(MathUtil.inverse(outer.get().getFullTransform()), window.getMousePosition());
                        var newOffset = (rel.getY() - yMouseDownOffset)/untrack(yScale);
                        yOffset.accept(-newOffset);
                    }));
                }
            });
        });

        return Nodes.single(Node.builder()
                .ref(node -> {
                    outer.set(node);

                    createEffect(() -> {
                        var viewSize = node.getLayout().getSize();
                        var contentSize = inner.get().getLayout().getSize();
                        yScale.accept(viewSize.getY() / contentSize.getY());
                    });

                    node.listen(
                            EventListener.onScroll(e -> {
                                var height = node.getLayout().getSize().getY();
                                var max = inner.get().getLayout().getSize().getY() - height;
                                yOffset.accept(v -> Math.min(0, Math.max(-max, v + e.getDeltaY())));
                            })
                    );
                })
                .layout(yoga -> {
                    Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
                    Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
                    Yoga.YGNodeStyleSetOverflow(yoga, Yoga.YGOverflowScroll);
                })
                .children(Nodes.multiple(
                        Node.builder()
                                .ref(inner::set)
                                .layout(yoga -> Yoga.YGNodeStyleSetWidthPercent(yoga, 100f))
                                .transform(node -> {

                                    var height = node.getParent().getLayout().getSize().getY();
                                    var max = node.getLayout().getSize().getY() - height;
                                    // TODO: bypass
                                    yOffset.accept(Math.min(0, Math.max(-max, yOffset.get())));
                                    return Matrix33.makeTranslate(0, yOffset.get());
                                })
                                .children(children)
                                .build(),
                        Node.builder()
                                .ref(node -> node.listen(
                                        onMouseOver(e -> mouseOver.accept(true)),
                                        onMouseOut(e -> mouseOver.accept(false)),
                                        onMouseDown(e -> {
                                            var pos = MathUtil.apply(MathUtil.inverse(node.getFullTransform()), window.getMousePosition());
                                            var maybeRect = barRect(node);
                                            if (maybeRect.isEmpty())
                                                return;

                                            var rect = maybeRect.get();
                                            if (MathUtil.contains(rect, pos)) {
                                                yMouseDownOffset = pos.getY() - rect.getTop();
                                                mouseDown.accept(true);
                                            }
                                        }),
                                        onMouseUp(e -> mouseDown.accept(false))
                                ))
                                .layout(Flex.builder()
                                        .width(15f)
                                        .heightPercent(100f)
                                        .absolute()
                                        .top(0)
                                        .right(0)
                                        .build()
                                )
                                .paint(this::paintScrollBar)
                                .build()
                ))
                .build()
        );
    }

    private Optional<Rect> barRect(MetaNode node) {
        if (yScale.get() < 1f) {
            var viewSize = node.getLayout().getSize();
            var bounds = Rect.makeWH(node.getLayout().getSize());
            return Optional.of(Rect.makeXYWH(
                    0,
                    yScale.get() * -yOffset.get(),
                    bounds.getWidth(),
                    yScale.get() * viewSize.getY()));
        } else {
            return Optional.empty();
        }
    }

    private void paintScrollBar(Canvas canvas, MetaNode node) {
        barRect(node).ifPresent(rect -> {
            try (var paint = new Paint()) {
                paint.setColor(EzColors.BLACK);
                canvas.drawRect(rect, paint);
            }
        });
    }
}
