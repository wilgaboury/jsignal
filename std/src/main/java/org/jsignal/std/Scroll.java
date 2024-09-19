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
import org.jsignal.rx.Ref;
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
import org.jsignal.ui.layout.LayoutConfig;
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
  Layouter outerLayout = CompositeLayouter.builder().fill().build();
  @Prop
  Layouter innerLayout = Layouter.empty();

  private final Signal<Float> xOffset = Signal.create(0f);
  private final Signal<Float> yOffset = Signal.create(0f);
  private final Signal<Boolean> xBarMouseDown = Signal.create(false);
  private final Signal<Boolean> yBarMouseDown = Signal.create(false);

  private float xMouseDownOffset = 0f;
  private float yMouseDownOffset = 0f;

  private final Signal<Boolean> xBarMouseOver = Signal.create(false);
  private final Signal<Boolean> yBarMouseOver = Signal.create(false);

  private final Signal<Float> xScale = Signal.create(Float.NaN);
  private final Signal<Float> yScale = Signal.create(Float.NaN);

  private final Ref<Node> content = new Ref<>();
  private final Ref<Node> view = new Ref<>();
  private final Ref<Node> xBar = new Ref<>();
  private final Ref<Node> yBar = new Ref<>();

  private Supplier<Boolean> shouldAddYBarSpace;
  private Supplier<Boolean> shouldAddXBarSpace;

  @Override
  protected void onBuild() {
    shouldAddYBarSpace = RxUtil.createMemo(() -> !overlay.get() && !yScale.get().isNaN() && yScale.get() < 1f);
    shouldAddXBarSpace = RxUtil.createMemo(() -> !overlay.get() && !xScale.get().isNaN() && xScale.get() < 1f);
  }

  @Override
  public Element render() {
    var window = UiWindow.context.use();

    onResolve(() -> {
      Effect.create(() -> {
        if (xBarMouseDown.get()) {
          var pos = window.getMousePosition();
          var rel = MathUtil.apply(MathUtil.inverse(xBar.get().getFullTransform()), pos);
          var newOffset = (rel.getX() - xMouseDownOffset) / ignore(xScale);
          xOffset.accept(-newOffset);
        }
      });

      Effect.create(() -> {
        if (yBarMouseDown.get()) {
          var pos = window.getMousePosition();
          var rel = MathUtil.apply(MathUtil.inverse(yBar.get().getFullTransform()), pos);
          var newOffset =
            (rel.getY() - yMouseDownOffset) / ignore(() -> yScale.get() * (yBar.get().getLayout().getHeight()
              / view.get().getLayout().getHeight()));
          setYOffset(-newOffset);
        }
      });

      Effect.create(() -> {
        var viewHeight = view.get().getLayout().getHeight();
        var contentHeight = content.get().getLayout().getHeight();
        yScale.accept(viewHeight / contentHeight);
        setYOffset(ignore(yOffset));
      });
    });

    return Node.builder()
      .ref(view)
      .listen(List.of(
        onScroll(e -> setYOffset(yOffset.get() + e.getDeltaY())),
        onKeyDown(e -> {
          if (e.getEvent().getKey() == Key.DOWN) {
            setYOffset(yOffset.get() - 100);
          } else if (e.getEvent().getKey() == Key.UP) {
            setYOffset(yOffset.get() + 100);
          }
        })
      ))
      .layout(config -> {
        outerLayout.layout(config);
        config.setOverflow(LayoutConfig.Overflow.SCROLL);
      })
      .children(Nodes.fromList(List.of(
        Node.builder()
          .ref(ref -> {
            content.accept(ref);
            ref.setPaintCacheStrategy(new UpgradingPaintCacheStrategy(SurfacePaintCacheStrategy::new));
          })
          .layout(config -> {
            innerLayout.layout(config);

            var lb = CompositeLayouter.builder();
            // may trigger second layout pass because shouldAddYBarSpace is based on yScale
            if (shouldAddYBarSpace.get()) {
              lb.padding(insets(0f, yBarWidth.get(), xBarWidth.get(), 0f).pixels());
            } else {
              lb.padding(insets(0f).pixels());
            }

            lb.build().layout(config);
          })
          .transform(layout -> Matrix33.makeTranslate(0f, yOffset.get()))
          .children(children.get())
          .build(),
        Node.builder()
          .ref(ref -> ref.listen(
            onMouseEnter(e -> xBarMouseOver.accept(true)),
            onMouseLeave(e -> xBarMouseOver.accept(false)),
            onMouseDown(e -> {
              var pos = MathUtil.apply(MathUtil.inverse(ref.getFullTransform()), window.getMousePosition());
              vertBarRect().ifPresent(rect -> {
                if (MathUtil.contains(rect, pos)) {
                  xMouseDownOffset = pos.getX() - rect.getLeft();
                  xBarMouseDown.accept(true);
                }
              });
            }),
            onMouseUp(e -> xBarMouseDown.accept(false))
          ))
          .layoutBuilder(lb -> lb
            .width(percent(100f))
            .height(() -> pixel(xBarWidth.get()))
            .absolute()
            .left(pixel(0f))
            .bottom(pixel(0f))
          )
          .paint(this::paintVertScrollBar)
          .build(),
        Node.builder()
          .listen(List.of(
            onMouseEnter(e -> yBarMouseOver.accept(true)),
            onMouseLeave(e -> yBarMouseOver.accept(false))
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
            new ScrollButton(upIcon, yBarWidth, this::yBarShow, () -> setYOffset(yOffset.get() + 100)),
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
            new ScrollButton(downIcon, yBarWidth, this::yBarShow, () -> setYOffset(yOffset.get() - 100)),
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

  private void setYOffset(Float value) {
    var height = view.get().getLayout().getHeight();
    var max = content.get().getLayout().getHeight() - height;
    var tmp = Math.min(0f, Math.max(-max, value));
    if (!Float.isNaN(tmp)) {
      yOffset.accept(tmp);
    }
  }

  private boolean yBarShow() {
    return shouldAddYBarSpace.get() || (overlay.get() && yBarMouseOver.get());
  }

  private Optional<Rect> vertBarRect() {
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
          onMouseEnter(e -> mouseOver.accept(true)),
          onMouseLeave(e -> mouseOver.accept(false))
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