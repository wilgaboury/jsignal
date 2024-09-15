package org.jsignal.std;

import io.github.humbleui.jwm.Key;
import io.github.humbleui.jwm.KeyModifier;
import io.github.humbleui.jwm.MouseCursor;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.paragraph.Affinity;
import io.github.humbleui.skija.paragraph.PositionWithAffinity;
import io.github.humbleui.skija.paragraph.RectHeightMode;
import io.github.humbleui.skija.paragraph.RectWidthMode;
import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Computed;
import org.jsignal.rx.Ref;
import org.jsignal.rx.Signal;
import org.jsignal.std.ez.EzColors;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.UiWindow;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.*;
import static org.jsignal.ui.event.EventListener.*;

@GeneratePropComponent
public non-sealed class InputLine extends InputLinePropComponent {
  @Prop
  Supplier<String> string;
  @Prop
  Consumer<String> onInput;

  private final Signal<Boolean> isFocused = Signal.create(false);

  private Para para;
  private final Signal<Optional<Integer>> cursorPosition = Signal.create(Optional.empty());

  @Override
  protected void onBuild() {
    this.para = Para.builder()
      .string(string)
      .styleBuilder(sb -> sb.maxLinesCount(1L))
      .line(true)
      .build();
  }

  @Override
  public Element render() {
    var ref = new Ref<Node>();

    var maybeWindow = UiWindow.context.use().getWindow();

    return Node.builder()
      .ref(ref)
      .listen(List.of(
        onMouseIn(event -> maybeWindow.ifPresent(window -> window.setMouseCursor(MouseCursor.IBEAM))),
        onMouseOut(event -> maybeWindow.ifPresent(window -> window.setMouseCursor(MouseCursor.ARROW))),
        onFocus(event -> isFocused.accept(true)),
        onBlur(event -> isFocused.accept(false)),
        onMouseClick(event -> {
          var pos = event.getPoint().getX();
          cursorPosition.accept(Optional.of(para.getParagraph().getGlyphPositionAtCoordinate(pos, 0).getPosition()));
        }),
        onKeyDown(event -> {
          if (event.getEvent().getKey() == Key.LEFT) {
            cursorPosition.accept(cur -> cur.map(v -> Math.max(0, v - 1)));
            return;
          } else if (event.getEvent().getKey() == Key.RIGHT) {
            var p = ignore(() -> para.getParagraph());
            var metrics = p.getLineMetrics()[0];
            int endPos = (int) metrics.getEndIndex();
            cursorPosition.accept(cur -> cur.map(v -> Math.min(endPos, v + 1)));
            return;
          }

          if (!event.getEvent().getKey().isLetterKey() || cursorPosition.get().isEmpty())
            return;

          var str = string.get();
          var split = cursorPosition.get().get();

          // TODO: sort out caps lock issue
          var insert = event.getEvent().isModifierDown(KeyModifier.SHIFT)
            ? event.getEvent().getKey().toString()
            : event.getEvent().getKey().toString().toLowerCase();

          batch(() -> {
            onInput.accept(str.substring(0, split) + insert + str.substring(split));
            cursorPosition.accept(cur -> cur.map(v -> v + 1));
          });
        })
      ))
      .paint((canvas, layout) -> {
        try (var paint = new Paint()) {
          paint.setColor(EzColors.BLUE_300);
          canvas.drawRect(layout.getBoundingRect(), paint);
        }
      })
      // TODO fix index check calculation in paint after
//      .layout(EzLayout.builder()
//        .border(insets(2f))
//        .padding(insets(pixel(4f)))
//        .build()
//      )
//      .paint((canvas, layout) -> {
//        if (isFocused.get()) {
//          try (var paint = new Paint()) {
//            paint.setColor(EzColors.SKY_500);
//            float radius = 4f;
//            canvas.drawDRRect(layout.getBorderRect().withRadii(radius),
//              layout.getPaddingRect().withRadii(radius), paint);
//          }
//        }
//      })
      .paintAfter((canvas, layout) -> {
        paintCursor(canvas);
      })
      .children(para)
      .build();
  }

  private void paintCursor(Canvas canvas) {
    cursorPosition.get().ifPresent(pos -> {
      try (var paint = new Paint()) {
        var p = ignore(() -> para.getParagraph());
        var metrics = p.getLineMetrics()[0];
        int endPos = (int) metrics.getEndIndex();
        var x = pos < endPos
          ? p.getRectsForRange(pos, pos + 1, RectHeightMode.MAX, RectWidthMode.MAX)[0].getRect().getLeft()
          : p.getRectsForRange(endPos - 1, endPos, RectHeightMode.MAX, RectWidthMode.MAX)[0].getRect().getRight();
        paint.setColor(EzColors.BLACK);
        paint.setAntiAlias(false);
        paint.setStrokeWidth(2f);
        canvas.drawLine(x, 0, x, (float) metrics.getLineHeight(), paint);
      }
    });
  }
}
