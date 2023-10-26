package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.YogaUtil;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createSignal;
import static com.github.wilgaboury.sigui.event.EventListener.onMouseOut;
import static com.github.wilgaboury.sigui.event.EventListener.onMouseOver;

public class Scroller extends Component {
    private final Nodes children;

    private final Signal<Float> yOffset;
//    private
    private final Signal<Boolean> overBar;

    public Scroller(Nodes children) {
        this.children = children;
        this.yOffset = createSignal(0f);
        this.overBar = createSignal(false);
    }

    @Override
    public Nodes render() {
        return Nodes.single(Node.builder()
                .layout(Flex.builder().stretch().build())
                .children(Nodes.fixed(
                        Node.builder()
                                .layout(yoga -> {
                                    Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
                                    Yoga.YGNodeStyleSetHeightAuto(yoga);
                                    Yoga.YGNodeStyleSetOverflow(yoga, Yoga.YGOverflowScroll);
                                })
                                .children(children)
                                .build(),
                        Node.builder()
                                .ref(node -> node.listen(
                                        onMouseOver(e -> overBar.accept(true)),
                                        onMouseOut(e -> overBar.accept(false))
                                ))
                                .layout(Flex.builder()
                                        .width(overBar.get() ? 15f : 5f)
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
            paint.setColor(EzColors.BLACK);
            canvas.drawRect(bounds, paint);
        }
    }
}
