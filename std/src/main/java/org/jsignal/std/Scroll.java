package org.jsignal.std;

import io.github.humbleui.jwm.Key;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.svg.SVGDOM;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Effect;
import org.jsignal.rx.RxUtil;
import org.jsignal.rx.Signal;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.ui.Component;
import org.jsignal.ui.Element;
import org.jsignal.ui.MathUtil;
import org.jsignal.ui.Node;
import org.jsignal.ui.Nodes;
import org.jsignal.ui.UiWindow;
import org.jsignal.ui.layout.CompositeLayouter;
import org.jsignal.ui.layout.Layout;
import org.jsignal.ui.layout.Layouter;
import org.jsignal.ui.paint.SurfacePaintCacheStrategy;
import org.jsignal.ui.paint.UpgradingPaintCacheStrategy;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.ignore;
import static org.jsignal.ui.Nodes.compose;
import static org.jsignal.ui.event.EventListener.*;
import static org.jsignal.ui.layout.Insets.insets;
import static org.jsignal.ui.layout.LayoutValue.percent;
import static org.jsignal.ui.layout.LayoutValue.pixel;

@GeneratePropComponent
public non-sealed class Scroll extends ScrollPropComponent {
  private static final float DEFAULT_WIDTH = 15f;

  private static final SVGDOM upIcon = IconUtil.fromStream(Scroll.class.getResourceAsStream("/icons/arrow-up-s-line.svg"));
  private static final SVGDOM downIcon = IconUtil.fromStream(Scroll.class.getResourceAsStream("/icons/arrow-down-s-line.svg"));

  @Prop
  Supplier<Boolean> overlay = Constant.of(false);
  @Prop
  Supplier<Float> barWidth = Constant.of(DEFAULT_WIDTH);
  @Prop
  Supplier<Float> xBarWidth = () -> barWidth.get();
  @Prop
  Supplier<Float> yBarWidth = () -> barWidth.get();
  @Prop
  Supplier<Float> yBarOverlayWidth = () -> yBarWidth.get() / 2f;
  @Prop
  Supplier<Element> children = Nodes::empty;
  @Prop
  Layouter layout = CompositeLayouter.builder()
    .width(percent(100f))
    .height(percent(100f))
    .overflow()
    .build();

  private final Signal<Float> xOffset = Signal.create(0f);
  private final Signal<Float> yOffset = Signal.create(0f);
  private final Signal<Boolean> xBarMouseDown = Signal.create(false);
  private final Signal<Boolean> yBarMouseDown = Signal.create(false);

  private float xMouseDownOffset = 0f;
  private float yMouseDownOffset = 0f;

  private final Signal<Boolean> xBarMouseOver = Signal.create(false);
  private final Signal<Boolean> yBarMouseOver = Signal.create(false);

  private final Signal<Node> content = Signal.empty();
  private final Signal<Node> view = Signal.empty();
  private final Signal<Node> xBar = Signal.empty();
  private final Signal<Node> yBar = Signal.empty();

  private final Signal<Float> xScale = Signal.create(Float.NaN);
  private final Signal<Float> yScale = Signal.create(Float.NaN);

  private Supplier<Boolean> shouldAddYBarSpace;
  private Supplier<Boolean> shouldAddXBarSpace;

  @Override
  protected void onBuild() {
    shouldAddYBarSpace = RxUtil.createMemo(() -> !yScale.get().isNaN() && yScale.get() < 1f && !overlay.get());
    shouldAddXBarSpace = RxUtil.createMemo(() -> !xScale.get().isNaN() && xScale.get() < 1f && !overlay.get());
  }

  @Override
  public Element render() {
    var window = UiWindow.context.use();

    Effect.create(() -> {
      if (xBar.get() != null && xBarMouseDown.get()) {
        var pos = window.getMousePosition();
        var rel = MathUtil.apply(MathUtil.inverse(xBar.get().getFullTransform()), pos);
        var newOffset = (rel.getX() - xMouseDownOffset) / ignore(xScale);
        xOffset.accept(-newOffset);
      }
    });

    Effect.create(() -> {
      if (yBar.get() != null && yBarMouseDown.get()) {
        var pos = window.getMousePosition();
        var rel = MathUtil.apply(MathUtil.inverse(yBar.get().getFullTransform()), pos);
        var newOffset =
          (rel.getY() - yMouseDownOffset) / ignore(() -> yScale.get() * (yBar.get().getLayout().getHeight()
            / view.get().getLayout().getHeight()));
        yOffset.accept(-newOffset);
      }
    });

    Effect.create(() -> {
      if (view.get() != null && content.get() != null) {
        var viewSize = view.get().getLayout().getSize();
        var contentSize = content.get().getLayout().getSize();
        yScale.accept(viewSize.getY() / contentSize.getY());
      }
    });

    return Node.builder()
      .ref(view)
      .listen(List.of(
        onScroll(e -> yOffset.accept(v -> v + e.getDeltaY())),
        onKeyDown(e -> {
          if (e.getEvent().getKey() == Key.DOWN) {
            yOffset.accept(y -> y - 100);
          } else if (e.getEvent().getKey() == Key.UP) {
            yOffset.accept(y -> y + 100);
          }
        })
      ))
      .layout(layout)
      .children(Nodes.fromList(List.of(
        Node.builder()
          .ref(meta -> {
            content.accept(meta);
            meta.setPaintCacheStrategy(new UpgradingPaintCacheStrategy(SurfacePaintCacheStrategy::new));
          })
          .layoutBuilder(lb -> {
            if (shouldAddYBarSpace.get()) {
              return lb
                .padding(insets(0f, yBarWidth.get(), xBarWidth.get(), 0f).pixels());
            } else {
              return lb
                .width(percent(100f))
                .padding(insets(0f).pixels());
            }
          })
          .transform(layout -> {
            var height = view.get().getLayout().getHeight();
            var max = layout.getHeight() - height;
            // TODO: enforce constraints on set
            var tmp = Math.min(0f, Math.max(-max, yOffset.get()));
            yOffset.accept(tmp);
            return Matrix33.makeTranslate(0f, yOffset.get());
          })
          .children(children.get())
          .build(),
        Node.builder()
          .ref(ref -> ref.listen(
            onMouseOver(e -> xBarMouseOver.accept(true)),
            onMouseOut(e -> xBarMouseOver.accept(false)),
            onMouseDown(e -> {
              var pos = MathUtil.apply(MathUtil.inverse(ref.getFullTransform()), window.getMousePosition());
              vertBarRect(ref).ifPresent(rect -> {
                if (MathUtil.contains(rect, pos)) {
                  xMouseDownOffset = pos.getX() - rect.getLeft();
                  xBarMouseDown.accept(true);
                }
              });
            }),
            onMouseUp(e -> xBarMouseDown.accept(false))
          ))
          .layout(EzLayout.builder()
            .width(percent(100f))
            .height(() -> pixel(xBarWidth.get()))
            .absolute()
            .left(pixel(0f))
            .bottom(pixel(0f))
            .build()
          )
          .paint(this::paintVertScrollBar)
          .build(),
        Node.builder()
          .listen(List.of(
            onMouseOver(e -> yBarMouseOver.accept(true)),
            onMouseOut(e -> yBarMouseOver.accept(false))
          ))
          .layout(EzLayout.builder()
            .width(() -> pixel(yBarWidth.get()))
            .height(percent(100f))
            .absolute()
            .top(pixel(0f))
            .right(pixel(0f))
            .build()
          )
          .children(compose(
            new ScrollButton(upIcon, yBarWidth, this::yBarShow, () -> yOffset.accept(y -> y + 100)),
            Node.builder()
              .ref(ref -> {
                yBar.accept(ref);
                ref.listen(
                  onMouseDown(e -> {
                    var pos = MathUtil.apply(
                      MathUtil.inverse(ref.getFullTransform()),
                      window.getMousePosition()
                    );
                    horizBarRect().ifPresent(rect -> {
                      if (MathUtil.contains(rect, pos)) {
                        yMouseDownOffset = pos.getY() - rect.getTop();
                        yBarMouseDown.accept(true);
                      }
                    });
                  }),
                  onMouseUp(e -> yBarMouseDown.accept(false))
                );
              })
              .layout(EzLayout.builder()
                .width(percent(100f))
                .grow(1f)
                .build()
              )
              .paint((canvas, node) -> paintHorizScrollBar(canvas))
              .build(),
            new ScrollButton(downIcon, yBarWidth, this::yBarShow, () -> yOffset.accept(y -> y - 100)),
            // spacer
            // TODO: put spacer only when x bar is showing
            Nodes.dynamic(() -> {
              if (shouldAddXBarSpace.get()) {
                return Node.builder()
                  .layout(EzLayout.builder().height(() -> pixel(xBarWidth.get())).build())
                  .build();
              } else {
                return Nodes.empty();
              }
            })
          ))
          .build()
      )))
      .build();
  }

  private boolean yBarShow() {
    return yBarMouseOver.get() || yBarMouseDown.get() || shouldAddYBarSpace.get();
  }

  private Optional<Rect> vertBarRect(Node meta) {
    return Optional.empty();
  }

  private void paintVertScrollBar(Canvas canvas, Layout layout) {
  }

  private Optional<Rect> horizBarRect() {
    if (yScale.get() < 1f) {
      var barHeight = yBar.get().getLayout().getHeight();
      var viewHeight = view.get().getLayout().getHeight();
      var secondScale = barHeight / viewHeight;
      return Optional.of(Rect.makeXYWH(
        0f,
        secondScale * yScale.get() * -yOffset.get(),
        yBar.get().getLayout().getWidth(),
        barHeight * yScale.get()
      ));
    } else {
      return Optional.empty();
    }
  }

  private void paintHorizScrollBar(Canvas canvas) {
    horizBarRect().ifPresent(rect -> {
      try (var paint = new Paint()) {
        paint.setColor(EzColors.GRAY_800);
        if (yBarShow()) {
          canvas.drawRRect(displayRect(rect), paint);
        } else {
          var smaller = rect.withLeft(yBarWidth.get() - yBarOverlayWidth.get());
          canvas.drawRRect(displayRect(smaller), paint);
        }
      }
    });
  }

  private static Rect shrinkRect(Rect rect, float amount) {
    return Rect.makeLTRB(
      rect.getLeft() + amount,
      rect.getTop() + amount,
      rect.getRight() - amount,
      rect.getBottom() - amount
    );
  }

  private static RRect displayRect(Rect rect) {
    return shrinkRect(rect, 1).withRadii(2f);
  }

  private static class ScrollButton extends Component {
    private final SVGDOM icon;
    private final Supplier<Float> size;
    private final Supplier<Boolean> show;
    private final Runnable action;

    public ScrollButton(
      SVGDOM icon,
      Supplier<Float> size,
      Supplier<Boolean> show,
      Runnable action
    ) {
      this.icon = icon;
      this.size = size;
      this.show = show;
      this.action = action;
    }

    private final Signal<Boolean> mouseDown = Signal.create(false);
    private final Signal<Boolean> mouseOver = Signal.create(false);

    @Override
    public Element render() {
      return Node.builder()
        .listen(List.of(
          onMouseClick(e -> action.run()),
          onMouseDown(e -> mouseDown.accept(true)),
          onMouseUp(e -> mouseDown.accept(false)),
          onMouseOver(e -> mouseOver.accept(true)),
          onMouseOut(e -> mouseOver.accept(false))
        ))
        .layout(EzLayout.builder()
          .height(() -> pixel(size.get()))
          .width(() -> pixel(size.get()))
          .build()
        )
        .paint((canvas, layout) -> {
          if (!show.get()) {
            return;
          }

          if (mouseDown.get()) {
            canvas.concat(
              Matrix33.makeTranslate(size.get() / 2f, size.get() / 2f)
                .makeConcat(Matrix33.makeScale(0.8f))
                .makeConcat(Matrix33.makeTranslate(-size.get() / 2f, -size.get() / 2f))
            );
          }

          if (mouseOver.get()) {
            try (var paint = new Paint()) {
              paint.setColor(EzColors.GRAY_200);
              var r = layout.getWidth() / 2f;
              canvas.drawCircle(r, r, r, paint);
            }
          }

          Image.paintSvg(canvas, layout, icon, Image.Fit.COVER);
        })
        .build();
    }
  }
}