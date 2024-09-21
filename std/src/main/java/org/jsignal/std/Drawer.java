package org.jsignal.std;

import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.skija.Paint;
import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.rx.*;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.ui.Element;
import org.jsignal.ui.HitTester;
import org.jsignal.ui.Node;
import org.jsignal.ui.Nodes;
import org.jsignal.ui.layout.LayoutConfig;

import java.util.List;
import java.util.function.Supplier;

import static org.jsignal.rx.Cleanups.onCleanup;
import static org.jsignal.rx.RxUtil.ignore;
import static org.jsignal.ui.event.EventListener.onMouseClick;
import static org.jsignal.ui.layout.LayoutValue.percent;
import static org.jsignal.ui.layout.LayoutValue.pixel;

@GeneratePropComponent
public non-sealed class Drawer extends DrawerPropComponent {
  @Prop(required = true)
  Supplier<Boolean> open;
  @Prop
  Supplier<Element> content = Nodes::empty;
  @Prop
  Supplier<Float> animDurationSeconds = Constant.of(0.2f);
  @Prop
  Supplier<TimingFunction> openAnimFunc = Constant.of(TimingFunction::linear);
  @Prop
  Runnable backgroundClick;

  @Override
  protected Element render() {
    Signal<Float> width = Signal.create(0f);
    var translation = createTranslation(width);

    return Node.builder()
      .layout(EzLayout.builder().absolute().left(pixel(0f)).top(pixel(0f)).fill().build())
      .hitTest((p, l) -> HitTester.Result.PASSTHROUGH)
      .children(Nodes.fromList(List.of(
        Node.builder()
          .layout(EzLayout.builder().fill().build())
          .listen(List.of(onMouseClick(e -> {
            if (backgroundClick != null) {
              backgroundClick.run();
            }
          })))
          .hitTest((point, layout) -> {
            if (open.get()) {
              return HitTester.boundsTest(point, layout);
            } else {
              return HitTester.Result.PASSTHROUGH;
            }
          })
          .paint((canvas, layout) -> {
            if (open.get()) {
              try (var paint = new Paint()) {
                paint.setColor(ColorUtil.withAlpha(EzColors.BLACK, 0.1f));
                canvas.drawRect(layout.getBoundingRect(), paint);
              }
            }
          })
          .build(),
        Node.builder()
          .layout(config -> {
            config.setPositionType(LayoutConfig.PositionType.ABSOLUTE);
            config.setPosition(LayoutConfig.Edge.LEFT, pixel(0f));
            config.setHeight(percent(100f));
          })
          .transform((layout) -> Matrix33.makeTranslate(translation.get().get(), 0))
          .ref(node -> Effect.create(() -> width.accept(node.getLayout().getWidth())))
          .children(content.get())
          .build()
      )))
      .build();
  }

  private Supplier<Supplier<Float>> createTranslation(Supplier<Float> width) {
    Ref<IntervalAnimation> prevAnimation = new Ref<>();
    Ref<Boolean> first = new Ref<>(true);
    return Computed.create(() -> {
      var o = open.get();

      var prev = prevAnimation.get();

      Supplier<Float> start = SkipMemo.from(() -> -width.get());
      Supplier<Float> end = Constant.of(0f);

      if (!o) {
        var tmp = start;
        start = end;
        end = tmp;
      }

      if (first.get()) {
        first.accept(false);
        return end;
      }

      if (prev != null) {
        start = Constant.of(ignore(prev));
      }

      var helper = IntervalAnimation.builder()
        .begin(start)
        .end(end)
        .durationSeconds(animDurationSeconds)
        .function(openAnimFunc)
        .build();

      prevAnimation.accept(helper);

      helper.start();
      onCleanup(helper::stop);

      return helper;
    });
  }
}
