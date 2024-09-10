package org.jsignal.std;

import io.github.humbleui.jwm.KeyModifier;
import io.github.humbleui.jwm.MouseCursor;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.paragraph.Affinity;
import io.github.humbleui.skija.paragraph.RectHeightMode;
import io.github.humbleui.skija.paragraph.RectWidthMode;
import org.jsignal.rx.Ref;
import org.jsignal.rx.RxUtil;
import org.jsignal.rx.Signal;
import org.jsignal.std.ez.EzColors;
import org.jsignal.std.ez.EzNode;
import org.jsignal.ui.Node;
import org.jsignal.ui.Element;
import org.jsignal.ui.Component;
import org.jsignal.ui.UiWindow;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.jsignal.ui.event.EventListener.*;

public class InputLine extends Component {
  private final Supplier<String> supplier;
  private final Consumer<String> consumer;

  private final Signal<Integer> cursorIndex = Signal.create(0);
  private final Signal<Optional<Float>> cursorPosition = Signal.create(Optional.empty());
  //  private final Signal<Boolean> cursorShow = Signal.create(false);
  private final Signal<Boolean> isFocused = Signal.create(false);

  private Para para;

  public InputLine(Supplier<String> supplier, Consumer<String> consumer) {
    this.supplier = RxUtil.createMemo(supplier);
    this.consumer = consumer;
  }

  @Override
  public Element render() {
    var ref = new Ref<Node>();

    para = Para.builder()
      .string(supplier)
      .customize(style -> style.maxLinesCount(1L))
      .line(true)
      .build();

    var maybeWindow = UiWindow.context.use().getWindow();

    return EzNode.builder()
      .ref(ref)
      .id("input-line")
      .listen(
        onMouseIn(event -> maybeWindow.ifPresent(window -> window.setMouseCursor(MouseCursor.IBEAM))),
        onMouseOut(event -> maybeWindow.ifPresent(window -> window.setMouseCursor(MouseCursor.ARROW))),
        onFocus(event -> isFocused.accept(true)),
        onBlur(event -> isFocused.accept(false)),
        onMouseClick(event -> cursorPosition.accept(Optional.of(event.getPoint().getX()))),
        onKeyDown(event -> {
          if (!event.getEvent().getKey().isLetterKey())
            return;

          var str = supplier.get();
          var split = cursorIndex.get();
          var insert = event.getEvent().getKey().toString();
          if (!event.getEvent().isModifierDown(KeyModifier.SHIFT))
            insert = insert.toLowerCase();
          consumer.accept(str.substring(0, split) + insert + str.substring(split));
        })
      )
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
        var paragraph = para.getParagraph();
        var aff = paragraph.getGlyphPositionAtCoordinate(pos, 0);
        var affPos = aff.getPosition() - (aff.getAffinity() == Affinity.DOWNSTREAM ? 0 : 1);
        var rect = paragraph.getRectsForRange(affPos, affPos + 1, RectHeightMode.MAX, RectWidthMode.MAX)[0].getRect();
        float middle = (rect.getLeft() + rect.getRight()) / 2f;
        var x = pos <= middle ? rect.getLeft() : rect.getRight();
        paint.setColor(EzColors.BLACK);
        paint.setAntiAlias(false);
        paint.setStrokeWidth(2f);
        canvas.drawLine(x, rect.getTop(), x, rect.getBottom(), paint);
      }
    });
  }
}
