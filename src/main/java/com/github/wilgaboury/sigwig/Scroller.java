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

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;
import static com.github.wilgaboury.sigui.event.EventListener.onMouseOut;
import static com.github.wilgaboury.sigui.event.EventListener.onMouseOver;

public class Scroller extends Component {
    private final Nodes children;

    private final Signal<Float> yOffset;
//    private final Signal<Float> xOffset;
    private final Signal<Boolean> overBar;

    public Scroller(Nodes children) {
        this.children = children;
        this.yOffset = createSignal(0f);
        this.overBar = createSignal(false);
    }

    @Override
    public Nodes render() {
        createEffect(() -> {
            System.out.println(overBar);
        });

        Ref<Long> outerYoga = new Ref<>();
        Ref<Long> innerYoga = new Ref<>();

        return Nodes.single(Node.builder()
                .ref(node -> node.listen(
                        EventListener.onScroll(e -> {
                            var height = Yoga.YGNodeLayoutGetHeight(node.getYoga());
                            var max = innerYoga.get() != null ? Yoga.YGNodeLayoutGetHeight(innerYoga.get()) - height: 0;
                            yOffset.accept(v -> Math.min(0, Math.max(-max, v + e.getDeltaY())));
                        })
                ))
                .layout(yoga -> {
                    outerYoga.set(yoga);
                    Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
                    Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
                    Yoga.YGNodeStyleSetOverflow(yoga, Yoga.YGOverflowScroll);
                })
                .children(Nodes.multiple(
                        Node.builder()
                                .layout(yoga -> {
                                    innerYoga.set(yoga);
                                    Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
                                })
                                .offset(yoga -> {
                                    var def = Node.defaultOffsetter(yoga);
                                    var height = Yoga.YGNodeLayoutGetHeight(outerYoga.get());
                                    var max = innerYoga.get() != null ? Yoga.YGNodeLayoutGetHeight(innerYoga.get()) - height: 0;
                                    // TODO: bypass
                                    yOffset.accept(Math.min(0, Math.max(-max, yOffset.get())));
                                    return def.makeConcat(Matrix33.makeTranslate(0, yOffset.get()));
                                })
                                .children(children)
                                .build(),
                        Node.builder()
                                .ref(node -> node.listen(
                                        onMouseOver(e -> overBar.accept(true)),
                                        onMouseOut(e -> overBar.accept(false))
                                ))
                                .layout(Flex.builder()
                                        .width(20f)
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

    private void paintScrollBar(Canvas canvas, long yoga) {
        Rect bounds = YogaUtil.boundingRect(yoga);

        try (var paint = new Paint()) {
            if (overBar.get()) {
                paint.setColor(EzColors.BLACK);
                canvas.drawRect(bounds, paint);
            } else {
                paint.setColor(EzColors.BLUE_300);
                canvas.drawRect(bounds.withLeft(bounds.getLeft() + 10), paint);
            }

        }
    }
}
