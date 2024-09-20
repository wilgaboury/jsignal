package org.jsignal.std;

import io.github.humbleui.jwm.Key;
import io.github.humbleui.jwm.KeyModifier;
import io.github.humbleui.jwm.MouseCursor;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Matrix33;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.paragraph.RectHeightMode;
import io.github.humbleui.skija.paragraph.RectWidthMode;
import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.prop.TransitiveProps;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Effect;
import org.jsignal.rx.Ref;
import org.jsignal.rx.Signal;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.ui.Element;
import org.jsignal.ui.MathUtil;
import org.jsignal.ui.Node;
import org.jsignal.ui.UiWindow;
import org.jsignal.ui.event.MouseEvent;
import org.jsignal.ui.layout.Layout;
import org.jsignal.ui.layout.Layouter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.batch;
import static org.jsignal.rx.RxUtil.ignore;
import static org.jsignal.ui.event.EventListener.*;
import static org.jsignal.ui.layout.Insets.insets;
import static org.jsignal.ui.layout.LayoutValue.pixel;

@GeneratePropComponent
public non-sealed class TextInput extends TextInputPropComponent {
  @Prop
  Supplier<String> content = Constant.of("");
  @Prop
  Consumer<String> onInput = (value) -> {};
  @Prop
  Layouter layout = Layouter.empty();
  @Prop
  Function<Para, Element> children = (para) -> para;

  @TransitiveProps
  public static class Transitive {
    Function<Supplier<String>, Para> toPara = Para::fromString;
  }

  private final Signal<Boolean> isFocused = Signal.create(false);

  private Para para;
  private final Ref<Node> ref = new Ref<>();
  private final Signal<Boolean> mouseDown = Signal.create(false);
  private final Signal<Optional<SelectionRange>> selectionRange = Signal.create(Optional.empty());

  @Override
  protected void onBuild(Transitive transitive) {
    this.para = transitive.toPara.apply(content);
  }

  @Override
  public Element render() {
    var window = UiWindow.context.use().getWindow().get();

    Effect.create(() -> {
      if (mouseDown.get()) {
        Effect.create(() -> {
          var pos = MathUtil.apply(
            MathUtil.inverse(para.getRef().getFullTransform()),
            UiWindow.context.use().getMousePosition()
          );
          var gPos = para.getParagraph().getGlyphPositionAtCoordinate(pos.getX(), pos.getY());
          selectionRange.accept(Optional.of(new SelectionRange(selectionRange.get().get().start, gPos.getPosition())));
        });
      }
    });

    return Node.builder()
      .ref(ref)
      .listen(List.of(
        onMouseEnter(event -> window.setMouseCursor(MouseCursor.IBEAM)),
        onMouseLeave(event -> window.setMouseCursor(MouseCursor.ARROW)),
        onFocus(event -> isFocused.accept(true)),
        onBlur(event -> {
          isFocused.accept(false);
          selectionRange.accept(Optional.empty());
        }),
        onMouseDown(event -> {
          var pos = mouseEventToPosition(event, ref.get());
          pos.ifPresent(p -> selectionRange.accept(Optional.of(new SelectionRange(p))));
          mouseDown.accept(true);
        }),
        onMouseUp(event -> mouseDown.accept(false)),
        onKeyDown(event -> {
          if (selectionRange.get().isEmpty()) {
            return;
          }

          var str = content.get();
          SelectionRange selection = selectionRange.get().get();

          var key = event.getEvent().getKey();

          if (key == Key.LEFT) {
            selectionRange.transform(cur -> cur.map(v -> new SelectionRange(
              selection.isPoint() ? Math.max(0, v.getMin() - 1) : selection.getMin()
            )));
          } else if (key == Key.RIGHT) {
            var p = ignore(() -> para.getParagraph());
            var lineMetrics = p.getLineMetrics();
            var lastLineMetrics = lineMetrics[lineMetrics.length - 1];
            int endPos = (int) lastLineMetrics.getEndIndex();
            selectionRange.transform(cur -> cur.map(v -> new SelectionRange(
              selection.isPoint() ? Math.min(endPos, v.getMax() + 1) : selection.getMax()
            )));
          } else if (key == Key.BACKSPACE) {
            if (selection.isPoint()) {
              if (selection.start > 0) {
                onInput.accept(str.substring(0, selection.start - 1) + str.substring(selection.start));
                selectionRange.transform(cur -> cur.map(v -> new SelectionRange(v.start - 1)));
              }
            } else {
              onInput.accept(str.substring(0, selection.getMin()) + str.substring(selection.getMax()));
              selectionRange.accept(Optional.of(new SelectionRange(selection.getMin())));
            }
          } else if (key == Key.DELETE) {
            if (selection.isPoint()) {
              if (selection.start < str.length()) {
                onInput.accept(str.substring(0, selection.start) + str.substring(selection.start + 1));
              }
            } else {
              onInput.accept(str.substring(0, selection.getMin()) + str.substring(selection.getMax()));
              selectionRange.accept(Optional.of(new SelectionRange(selection.getMin())));
            }
          } else {
            var insert = keyToInputString(key, !event.getEvent().isModifierDown(KeyModifier.SHIFT));

            if (insert == null) {
              return;
            }

            batch(() -> {
              onInput.accept(str.substring(0, selection.getMin()) + insert + str.substring(selection.getMax()));
              selectionRange.transform(cur -> cur.map(v -> new SelectionRange(v.getMin() + 1)));
            });
          }
        })
      ))
      .layout(EzLayout.builder()
        .border(insets(2f))
        .padding(insets(pixel(4f)))
        .build()
      )
      .paint((canvas, layout) -> {
        try (var paint = new Paint()) {
          paint.setColor(isFocused.get() ? EzColors.SKY_500 : EzColors.BLACK);
          float radius = 4f;
          canvas.drawDRRect(layout.getBorderRect().withRadii(radius),
            layout.getPaddingRect().withRadii(radius), paint
          );
        }

        paintSelection(canvas, layout);
      })
      .paintAfter(this::paintCursor)
      .children(children.apply(para))
      .build();
  }

  private Optional<Integer> mouseEventToPosition(MouseEvent e, Node node) {
    if (content.get().isEmpty()) {
      return Optional.of(0);
    } else {
      var pos = e.getPoint();
      var content = node.getLayout().getContentRect();
      var x = pos.getX() - content.getLeft();
      var y = pos.getY() - content.getTop();
      return Optional.of(para.getParagraph().getGlyphPositionAtCoordinate(x, y).getPosition());
    }
  }

  private void paintCursor(Canvas canvas, Layout layout) {
    var maybeSelection = selectionRange.get();
    if (maybeSelection.isPresent() && maybeSelection.get().isPoint()) {
      var pos = maybeSelection.get().start;
      try (var paint = new Paint()) {
        paint.setColor(EzColors.BLACK);
        paint.setAntiAlias(false);
        paint.setStrokeWidth(2f);

        if (ignore(content).isEmpty()) {
          var x = layout.getContentRect().getLeft();
          var y = layout.getContentRect().getTop();
          canvas.drawLine(x, y, x, y + layout.getContentRect().getHeight(), paint);
          return;
        }

        var p = para.getParagraph();
        var lineMetrics = p.getLineMetrics();
        var lastLineMetrics = lineMetrics[lineMetrics.length - 1];
        int endPos = (int) lastLineMetrics.getEndIndex();
        var rect = pos < endPos
          ? p.getRectsForRange(pos, pos + 1, RectHeightMode.MAX, RectWidthMode.MAX)[0].getRect()
          : p.getRectsForRange(endPos - 1, endPos, RectHeightMode.MAX, RectWidthMode.MAX)[0].getRect();
        var x = layout.getContentRect().getLeft() + (pos < endPos ? rect.getLeft() : rect.getRight());
        var yOffset = layout.getContentRect().getTop();
        canvas.drawLine(x, yOffset + rect.getTop(), x, yOffset + rect.getBottom(), paint);
      }
    }
  }

  private void paintSelection(Canvas canvas, Layout layout) {
    selectionRange.get().ifPresent(range -> {
      if (range.start == range.end) {
        return;
      }

      var p = para.getParagraph();
      var rects = p.getRectsForRange(range.getMin(), range.getMax(), RectHeightMode.MAX, RectWidthMode.MAX);
      var contentRect = layout.getContentRect();
      int count = canvas.save();
      try {
        canvas.concat(Matrix33.makeTranslate(contentRect.getLeft(), contentRect.getTop()));
        try (var paint = new Paint()) {
          paint.setColor(EzColors.BLUE_400);
          for (var rect : rects) {
            canvas.drawRect(rect.getRect(), paint);
          }
        }
      } finally {
        canvas.restoreToCount(count);
      }
    });
  }

  private record SelectionRange(int start, int end) {
    public SelectionRange(int value) {
      this(value, value);
    }

    public SelectionRange(SelectionRange prev, int newEnd) {
      this(prev.start, newEnd);
    }

    public int getMin() {
      return Math.min(start, end);
    }

    public int getMax() {
      return Math.max(start, end);
    }

    public boolean isPoint() {
      return start == end;
    }
  }

  public static String keyToInputString(Key key, boolean lower) {
    if (lower) {
      return switch (key) {
        case SPACE -> " ";
        case COMMA -> ",";
        case MINUS -> "-";
        case PERIOD -> ".";
        case SLASH -> "/";
        case SEMICOLON -> ";";
        case EQUALS -> "=";
        case OPEN_BRACKET -> "[";
        case BACK_SLASH -> "\\";
        case CLOSE_BRACKET -> "]";
        case MULTIPLY -> "*";
        case ADD -> "+";
        case SEPARATOR -> "|";
        case BACK_QUOTE -> "`";
        case QUOTE -> "\"";
        default -> {
          if (key.isDigitKey() || key.isLetterKey()) {
            yield key.getName().toLowerCase();
          } else {
            yield null;
          }
        }
      };
    } else {
      return switch (key) {
        case SPACE -> " ";
        case COMMA -> "<";
        case MINUS -> "_";
        case PERIOD -> ">";
        case SLASH -> "?";
        case SEMICOLON -> ":";
        case EQUALS -> "+";
        case OPEN_BRACKET -> "{";
        case BACK_SLASH -> "|";
        case CLOSE_BRACKET -> "}";
        case BACK_QUOTE -> "~";
        case QUOTE -> "'";
        case DIGIT1 -> "!";
        case DIGIT2 -> "@";
        case DIGIT3 -> "#";
        case DIGIT4 -> "$";
        case DIGIT5 -> "%";
        case DIGIT6 -> "^";
        case DIGIT7 -> "&";
        case DIGIT8 -> "*";
        case DIGIT9 -> "(";
        case DIGIT0 -> ")";
        default -> {
          if (key.isLetterKey()) {
            yield key.getName();
          } else {
            yield null;
          }
        }
      };
    }
  }
}
