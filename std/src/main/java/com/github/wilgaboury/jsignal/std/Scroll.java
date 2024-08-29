package com.github.wilgaboury.jsignal.std;

import com.github.wilgaboury.jsignal.rx.Computed;
import com.github.wilgaboury.jsignal.rx.Ref;
import com.github.wilgaboury.jsignal.rx.Signal;
import com.github.wilgaboury.jsignal.std.ez.EzColors;
import com.github.wilgaboury.jsignal.std.ez.EzLayout;
import com.github.wilgaboury.jsignal.std.ez.EzNode;
import com.github.wilgaboury.jsignal.ui.*;
import com.github.wilgaboury.jsignal.ui.layout.Layout;
import com.github.wilgaboury.jsignal.ui.paint.SurfacePaintCacheStrategy;
import com.github.wilgaboury.jsignal.ui.paint.UpgradingPaintCacheStrategy;
import io.github.humbleui.jwm.Key;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;

import java.util.Optional;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.rx.RxUtil.untrack;
import static com.github.wilgaboury.jsignal.ui.UiUtil.createEffectLater;
import static com.github.wilgaboury.jsignal.ui.event.EventListener.*;
import static com.github.wilgaboury.jsignal.ui.layout.Insets.insets;
import static com.github.wilgaboury.jsignal.ui.layout.LayoutValue.percent;
import static com.github.wilgaboury.jsignal.ui.layout.LayoutValue.pixel;

public class Scroll implements Renderable {
  private static final float DEFAULT_WIDTH = 15f;

  private final Supplier<Boolean> overlay;
  private final Supplier<Float> xBarWidth;
  private final Supplier<Float> yBarWidth;
  private final Supplier<Float> yBarOverlayWidth;
  private final Supplier<Nodes> children;

  private final Signal<Float> xOffset = Signal.create(0f);
  private final Signal<Float> yOffset = Signal.create(0f);
  private final Signal<Boolean> xBarMouseDown = Signal.create(false);
  private final Signal<Boolean> yBarMouseDown = Signal.create(false);

  private float xMouseDownOffset = 0f;
  private float yMouseDownOffset = 0f;

  private final Signal<Boolean> xBarMouseOver = Signal.create(false);
  private final Signal<Boolean> yBarMouseOver = Signal.create(false);

  private final Ref<MetaNode> content = new Ref<>();
  private final Ref<MetaNode> view = new Ref<>();
  private final Ref<MetaNode> xBar = new Ref<>();
  private final Ref<MetaNode> yBar = new Ref<>();

  private final Signal<Float> xScale = Signal.create(0f);
  private final Signal<Float> yScale = Signal.create(0f);
  private final Computed<Boolean> shouldShowSidebar;

  public Scroll(Builder builder) {
    this.overlay = builder.overlay;
    this.xBarWidth = builder.xBarWidth;
    this.yBarWidth = builder.yBarWidth;
    this.yBarOverlayWidth = builder.yBarOverlayWidth;
    this.children = builder.children;

    shouldShowSidebar = Computed.create(() -> (yScale.get().isNaN() || yScale.get() < 1f) && !overlay.get());
  }

  @Override
  public NodesSupplier render() {
    var window = UiWindow.context.use();

    createEffectLater(() -> {
      if (xBarMouseDown.get()) {
        var pos = window.getMousePosition();
        var rel = MathUtil.apply(MathUtil.inverse(xBar.get().getFullTransform()), pos);
        var newOffset = (rel.getX() - xMouseDownOffset) / untrack(xScale);
        xOffset.accept(-newOffset);
      }
    });

    createEffectLater(() -> {
      if (yBarMouseDown.get()) {
        var pos = window.getMousePosition();
        var rel = MathUtil.apply(MathUtil.inverse(yBar.get().getFullTransform()), pos);
        var newOffset =
          (rel.getY() - yMouseDownOffset) / untrack(() -> yScale.get() * (yBar.get().getLayout().getHeight()
            / view.get().getLayout().getHeight()));
        yOffset.accept(-newOffset);
      }
    });

    createEffectLater(() -> {
      var viewSize = view.get().getLayout().getSize();
      var contentSize = content.get().getLayout().getSize();
      yScale.accept(viewSize.getY() / contentSize.getY());
    });

    return EzNode.builder()
      .ref(view)
      .listen(
        onScroll(e -> yOffset.accept(v -> v + e.getDeltaY())),
        onKeyDown(e -> {
          if (e.getEvent().getKey() == Key.DOWN) {
            yOffset.accept(y -> y - 100);
          } else if (e.getEvent().getKey() == Key.UP) {
            yOffset.accept(y -> y + 100);
          }
        })
      )
      .layout(EzLayout.builder()
        .width(percent(100f))
        .height(percent(100f))
        .overflow()
        .build()
      )
      .children(
        EzNode.builder()
          .ref(meta -> {
            content.accept(meta);
            meta.setPaintCacheStrategy(new UpgradingPaintCacheStrategy(SurfacePaintCacheStrategy::new));
          })
          .layout(yoga -> {
            // TODO: this might be bad, requires multiple layout passes hack because this signal is reacting to layout signals
            if (shouldShowSidebar.get()) {
              EzLayout.builder()
                .padding(insets(0f, yBarWidth.get(), xBarWidth.get(), 0f).toLayout())
                .build()
                .layout(yoga);
            } else {
              EzLayout.builder()
                .width(percent(100f))
                .padding(insets(pixel(0f)))
                .build()
                .layout(yoga);
            }
          })
          .transform(layout -> {
            var height = view.get().getLayout().getHeight();
            var max = layout.getHeight() - height;
            // TODO: bypass
            var tmp = Math.min(0f, Math.max(-max, yOffset.get()));
            yOffset.accept(tmp);
            return Matrix33.makeTranslate(0f, yOffset.get());
          })
          .children(children.get())
          .build(),
        EzNode.builder()
          .ref(meta -> meta.listen(
            onMouseOver(e -> xBarMouseOver.accept(true)),
            onMouseOut(e -> xBarMouseOver.accept(false)),
            onMouseDown(e -> {
              var pos = MathUtil.apply(MathUtil.inverse(meta.getFullTransform()), window.getMousePosition());
              vertBarRect(meta).ifPresent(rect -> {
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
        EzNode.builder()
          .listen(
            onMouseOver(e -> yBarMouseOver.accept(true)),
            onMouseOut(e -> yBarMouseOver.accept(false))
          )
          .layout(EzLayout.builder()
            .width(() -> pixel(yBarWidth.get()))
            .height(percent(100f))
            .absolute()
            .top(pixel(0f))
            .right(pixel(0f))
            .build()
          )
          .children(
            new ScrollButton(yBarWidth, this::yBarShow, () -> yOffset.accept(y -> y + 100)),
            EzNode.builder()
              .ref(meta -> {
                yBar.accept(meta);
                meta.listen(
                  onMouseDown(e -> {
                    var pos = MathUtil.apply(
                      MathUtil.inverse(meta.getFullTransform()),
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
            new ScrollButton(yBarWidth, this::yBarShow, () -> yOffset.accept(y -> y - 100)),
            // spacer
            EzNode.builder()
              .layout(EzLayout.builder().height(() -> pixel(xBarWidth.get())).build())
              .build()
          )
          .build()
      )
      .build();
  }

  private boolean yBarShow() {
    return yBarMouseOver.get() || yBarMouseDown.get() || !overlay.get();
  }

  private Optional<Rect> vertBarRect(MetaNode meta) {
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
        paint.setColor(EzColors.BLACK);
        if (yBarShow()) {
          canvas.drawRect(rect, paint);
        } else {
          var smaller = rect.withLeft(yBarWidth.get() - yBarOverlayWidth.get());
          canvas.drawRect(smaller, paint);
        }
      }
    });
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Supplier<Boolean> overlay = () -> false;
    private Supplier<Float> barWidth = () -> DEFAULT_WIDTH;
    private Supplier<Float> xBarWidth = barWidth;
    private Supplier<Float> yBarWidth = barWidth;
    private Supplier<Float> yBarOverlayWidth = () -> yBarWidth.get() / 2f;
    private Supplier<Nodes> children = Nodes::empty;

    public Supplier<Boolean> getOverlay() {
      return overlay;
    }

    public Builder setOverlay(boolean overlay) {
      return setOverlay(() -> overlay);
    }


    public Builder setOverlay(Supplier<Boolean> overlay) {
      this.overlay = overlay;
      return this;
    }

    public Supplier<Float> getBarWidth() {
      return barWidth;
    }

    public Builder setBarWidth(float barWidth) {
      return setBarWidth(() -> barWidth);
    }

    public Builder setBarWidth(Supplier<Float> barWidth) {
      this.barWidth = barWidth;
      return this;
    }

    public Supplier<Float> getXBarWidth() {
      return xBarWidth;
    }

    public Builder setXBarWidth(float xBarWidth) {
      return setXBarWidth(() -> xBarWidth);
    }

    public Builder setXBarWidth(Supplier<Float> xBarWidth) {
      this.xBarWidth = xBarWidth;
      return this;
    }

    public Supplier<Float> getYBarWidth() {
      return yBarWidth;
    }

    public Builder setYBarWidth(float yBarWidth) {
      return setYBarWidth(() -> yBarWidth);
    }

    public Builder setYBarWidth(Supplier<Float> yBarWidth) {
      this.yBarWidth = yBarWidth;
      return this;
    }

    public Supplier<Float> getYBarOverlayWidth() {
      return yBarOverlayWidth;
    }

    public Builder setYBarOverlayWidth(float yBarOverlayWidth) {
      return setYBarOverlayWidth(() -> yBarOverlayWidth);
    }

    public Builder setYBarOverlayWidth(Supplier<Float> yBarOverlayWidth) {
      this.yBarOverlayWidth = yBarOverlayWidth;
      return this;
    }

    public Supplier<Nodes> getChildren() {
      return children;
    }

    public Builder setChildren(Nodes children) {
      return setChildren(() -> children);
    }

    public Builder setChildren(Supplier<Nodes> children) {
      this.children = children;
      return this;
    }

    public Scroll build() {
      return new Scroll(this);
    }
  }

  private static class ScrollButton implements Renderable {
    private final Supplier<Float> size;
    private final Supplier<Boolean> show;
    private final Runnable action;

    public ScrollButton(
      Supplier<Float> size,
      Supplier<Boolean> show,
      Runnable action
    ) {
      this.size = size;
      this.show = show;
      this.action = action;
    }

    private final Signal<Boolean> mouseDown = Signal.create(false);

    @Override
    public NodesSupplier render() {
      return EzNode.builder()
        .listen(
          onMouseClick(e -> action.run()),
          onMouseDown(e -> mouseDown.accept(true)),
          onMouseUp(e -> mouseDown.accept(false))
        )
        .layout(EzLayout.builder()
          .height(() -> pixel(size.get()))
          .width(() -> pixel(size.get()))
          .build()
        )
        .transform(node -> {
          if (!mouseDown.get()) {
            return Matrix33.IDENTITY;
          } else {
            return Matrix33.makeTranslate(size.get() / 2f, size.get() / 2f)
              .makeConcat(Matrix33.makeScale(0.8f))
              .makeConcat(Matrix33.makeTranslate(-size.get() / 2f, -size.get() / 2f));
          }
        })
        .paint((canvas, layout) -> {
          if (!show.get()) {
            return;
          }

          try (var paint = new Paint()) {
            paint.setColor(EzColors.BLUE_300);
            canvas.drawRect(layout.getBoundingRect(), paint);
          }
        })
        .build();
    }
  }
}