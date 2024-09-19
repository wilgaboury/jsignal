package org.jsignal.std;

import io.github.humbleui.jwm.Key;
import io.github.humbleui.jwm.KeyModifier;
import io.github.humbleui.jwm.MouseCursor;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.paragraph.RectHeightMode;
import io.github.humbleui.skija.paragraph.RectWidthMode;
import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.prop.TransitiveProps;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Ref;
import org.jsignal.rx.Signal;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzLayout;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.UiWindow;
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
  private final Signal<Optional<Integer>> cursorPosition = Signal.create(Optional.empty());

  @Override
  protected void onBuild(Transitive transitive) {
    this.para = transitive.toPara.apply(content);
  }

  @Override
  public Element render() {
    var ref = new Ref<Node>();

    var window = UiWindow.context.use().getWindow().get();

    return Node.builder()
      .ref(ref)
      .listen(List.of(
        onMouseEnter(event -> window.setMouseCursor(MouseCursor.IBEAM)),
        onMouseLeave(event -> window.setMouseCursor(MouseCursor.ARROW)),
        onFocus(event -> {
          isFocused.accept(true);
          if (cursorPosition.get().isEmpty()) {
            cursorPosition.accept(Optional.of(0));
          }
        }),
        onBlur(event -> {
          isFocused.accept(false);
          cursorPosition.accept(Optional.empty());
        }),
        onMouseClick(event -> {
          if (content.get().isEmpty()) {
            cursorPosition.accept(Optional.of(0));
          } else {
            var pos = event.getPoint();
            var content = ref.get().getLayout().getContentRect();
            var x = pos.getX() - content.getLeft();
            var y = pos.getY() - content.getTop();
            cursorPosition.accept(Optional.of(para.getParagraph().getGlyphPositionAtCoordinate(x, y).getPosition()));
          }
        }),
        onKeyDown(event -> {
          if (cursorPosition.get().isEmpty()) {
            return;
          }

          var str = content.get();
          int split = cursorPosition.get().get();

          var key = event.getEvent().getKey();

          if (key == Key.LEFT) {
            cursorPosition.transform(cur -> cur.map(v -> Math.max(0, v - 1)));
            return;
          } else if (key == Key.RIGHT) {
            var p = ignore(() -> para.getParagraph());
            var lineMetrics = p.getLineMetrics();
            var lastLineMetrics = lineMetrics[lineMetrics.length - 1];
            int endPos = (int) lastLineMetrics.getEndIndex();
            cursorPosition.transform(cur -> cur.map(v -> Math.min(endPos, v + 1)));
            return;
          } else if (key == Key.BACKSPACE && split > 0) {
            onInput.accept(str.substring(0, split - 1) + str.substring(split));
            cursorPosition.transform(cur -> cur.map(v -> v - 1));
          } else if (key == Key.DELETE && split < str.length()) {
            onInput.accept(str.substring(0, split) + str.substring(split + 1));
          }

          var insert = keyToInputString(key, !event.getEvent().isModifierDown(KeyModifier.SHIFT));

          if (insert == null) {
            return;
          }

          batch(() -> {
            onInput.accept(str.substring(0, split) + insert + str.substring(split));
            cursorPosition.transform(cur -> cur.map(v -> v + 1));
          });
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
      })
      .paintAfter(this::paintCursor)
      .children(children.apply(para))
      .build();
  }

  private void paintCursor(Canvas canvas, Layout layout) {
    cursorPosition.get().ifPresent(pos -> {
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

        var p = ignore(() -> para.getParagraph());
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
    });
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
