package com.github.wilgaboury.jsigwig;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigui.event.EventListener;
import io.github.humbleui.jwm.Key;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import java.util.Optional;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;
import static com.github.wilgaboury.sigui.SiguiUtil.createEffectLater;

@JSiguiComponent
public class Scroll implements Renderable {
  private static final float DEFAULT_WIDTH = 15f;

  private final Supplier<Boolean> overlay;
  private final Supplier<Float> xBarWidth;
  private final Supplier<Float> yBarWidth;
  private final Supplier<Float> yBarOverlayWidth;
  private final Supplier<NodesSupplier> children;

  private final Signal<Float> xOffset = createSignal(0f);
  private final Signal<Float> yOffset = createSignal(0f);
  private final Signal<Boolean> xBarMouseDown = createSignal(false);
  private final Signal<Boolean> yBarMouseDown = createSignal(false);

  private float xMouseDownOffset = 0f;
  private float yMouseDownOffset = 0f;

  private final Signal<Boolean> xBarMouseOver = createSignal(false);
  private final Signal<Boolean> yBarMouseOver = createSignal(false);

  private final Ref<MetaNode> content = new Ref<>();
  private final Ref<MetaNode> view = new Ref<>();
  private final Ref<MetaNode> xBar = new Ref<>();
  private final Ref<MetaNode> yBar = new Ref<>();

  private final Signal<Float> xScale = createSignal(0f);
  private final Signal<Float> yScale = createSignal(0f);
  private final Computed<Boolean> shouldShowSidebar;

  public Scroll(Builder builder) {
    this.overlay = builder.overlay;
    this.xBarWidth = builder.xBarWidth;
    this.yBarWidth = builder.yBarWidth;
    this.yBarOverlayWidth = builder.yBarOverlayWidth;
    this.children = builder.children;

    shouldShowSidebar = createComputed(() -> (yScale.get().isNaN() || yScale.get() < 1f) && !overlay.get());
  }

  @Override
  public Nodes render() {
    var window = SiguiWindow.useWindow();

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

    return Node.builder()
      .reference(node -> {
        view.set(node);
        node.tags("scroller-parent");
        node.listen(
          EventListener.onScroll(e -> yOffset.accept(v -> v + e.getDeltaY())),
          EventListener.onKeyDown(e -> {
            if (e.getEvent().getKey() == Key.DOWN) {
              yOffset.accept(y -> y - 100);
            } else if (e.getEvent().getKey() == Key.UP) {
              yOffset.accept(y -> y + 100);
            }
          })
        );
      })
      .layout(Flex.builder()
        .widthPercent(100f)
        .heightPercent(100f)
        .overflow(Yoga.YGOverflowScroll)
        .build()
      )
      .children(
        Node.builder()
          .reference(content::set)
          .layout(yoga -> {
            // TODO: this might be bad, requires multiple layout passes hack because this signal is reacting to layout signals
            if (shouldShowSidebar.get()) {
              Flex.builder()
                .padding(new Insets(0f, yBarWidth.get(), xBarWidth.get(), 0f))
                .build()
                .layout(yoga);
            } else {
              Flex.builder()
                .widthPercent(100f)
                .padding(new Insets(0f))
                .build()
                .layout(yoga);
            }
          })
          .transform(node -> {
            var height = node.getParent().getLayout().getHeight();
            var max = node.getLayout().getHeight() - height;
            // TODO: bypass
            yOffset.accept(Math.min(0f, Math.max(-max, yOffset.get())));
            return Matrix33.makeTranslate(0f, yOffset.get());
          })
          .children(children.get())
          .build(),
        Node.builder()
          .reference(node -> node.listen(
            EventListener.onMouseOver(e -> xBarMouseOver.accept(true)),
            EventListener.onMouseOut(e -> xBarMouseOver.accept(false)),
            EventListener.onMouseDown(e -> {
              var pos = MathUtil.apply(MathUtil.inverse(node.getFullTransform()), window.getMousePosition());
              vertBarRect(node).ifPresent(rect -> {
                if (MathUtil.contains(rect, pos)) {
                  xMouseDownOffset = pos.getX() - rect.getLeft();
                  xBarMouseDown.accept(true);
                }
              });
            }),
            EventListener.onMouseUp(e -> xBarMouseDown.accept(false))
          ))
          .layout(Flex.builder()
            .widthPercent(100f)
            .height(xBarWidth.get())
            .absolute()
            .left(0f)
            .bottom(0f)
            .build()
          )
          .paint(this::paintVertScrollBar)
          .build(),
        Node.builder()
          .reference(node -> node.listen(
            EventListener.onMouseOver(e -> yBarMouseOver.accept(true)),
            EventListener.onMouseOut(e -> yBarMouseOver.accept(false))
          ))
          .layout(Flex.builder()
            .width(yBarWidth.get())
            .heightPercent(100f)
            .absolute()
            .top(0f)
            .right(0f)
            .build()
          )
          .children(
            new ScrollButton(yBarWidth, this::yBarShow, () -> yOffset.accept(y -> y + 100)),
            Node.builder()
              .reference(node -> {
                yBar.set(node);
                node.listen(
                  EventListener.onMouseDown(e -> {
                    var pos = MathUtil.apply(
                      MathUtil.inverse(node.getFullTransform()),
                      window.getMousePosition()
                    );
                    horizBarRect().ifPresent(rect -> {
                      if (MathUtil.contains(rect, pos)) {
                        yMouseDownOffset = pos.getY() - rect.getTop();
                        yBarMouseDown.accept(true);
                      }
                    });
                  }),
                  EventListener.onMouseUp(e -> yBarMouseDown.accept(false))
                );
              })
              .layout(Flex.builder()
                .widthPercent(100f)
                .grow(1f)
                .build()
              )
              .paint((canvas, node) -> paintHorizScrollBar(canvas))
              .build(),
            new ScrollButton(yBarWidth, this::yBarShow, () -> yOffset.accept(y -> y - 100)),
            // spacer
            Node.builder()
              .layout(yoga -> Flex.builder()
                .height(xBarWidth.get())
                .build()
                .layout(yoga)
              )
              .build()
          )
          .build()
      )
      .build();
  }

  private boolean yBarShow() {
    return yBarMouseOver.get() || yBarMouseDown.get() || !overlay.get();
  }

  private Optional<Rect> vertBarRect(MetaNode node) {
    return Optional.empty();
  }

  private void paintVertScrollBar(Canvas canvas, MetaNode node) {
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
    private Supplier<NodesSupplier> children = Nodes::empty;

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

    public Supplier<NodesSupplier> getChildren() {
      return children;
    }

    public Builder setChildren(NodesSupplier children) {
      return setChildren(() -> children);
    }

    public Builder setChildren(Supplier<NodesSupplier> children) {
      this.children = children;
      return this;
    }

    public Scroll build() {
      return new Scroll(this);
    }
  }

  @JSiguiComponent
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

    private final Signal<Boolean> mouseDown = createSignal(false);

    @Override
    public Nodes render() {
      return Node.builder()
        .reference(reference -> reference.listen(
          EventListener.onMouseClick(e -> action.run()),
          EventListener.onMouseDown(e -> mouseDown.accept(true)),
          EventListener.onMouseUp(e -> mouseDown.accept(false))
        ))
        .layout(yoga -> Flex.builder()
          .height(size.get())
          .width(size.get())
          .build()
          .layout(yoga)
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
        .paint((canvas, node) -> {
            if (!show.get()) {
                return;
            }

          try (var paint = new Paint()) {
            paint.setColor(EzColors.BLUE_300);
            canvas.drawRect(node.getLayout().getBoundingRect(), paint);
          }
        })
        .build();
    }
  }
}