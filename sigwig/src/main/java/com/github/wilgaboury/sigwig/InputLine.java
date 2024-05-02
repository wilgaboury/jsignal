package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.JSignalUtil;
import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.MetaNode;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.SiguiComponent;
import com.github.wilgaboury.sigwig.ez.EzColors;
import com.github.wilgaboury.sigwig.ez.EzNode;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.paragraph.Affinity;
import io.github.humbleui.skija.paragraph.PositionWithAffinity;
import io.github.humbleui.skija.paragraph.RectHeightMode;
import io.github.humbleui.skija.paragraph.RectWidthMode;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.wilgaboury.sigui.event.EventListener.*;

@SiguiComponent
public class InputLine implements Renderable {
  private final Supplier<String> supplier;
  private final Consumer<String> consumer;

  private final Signal<Optional<Float>> cursorPosition = Signal.create(Optional.empty());
  //  private final Signal<Boolean> cursorShow = Signal.create(false);
  private final Signal<Boolean> isFocused = Signal.create(false);

  public InputLine(Supplier<String> supplier, Consumer<String> consumer) {
    this.supplier = JSignalUtil.maybeComputed(supplier);
    this.consumer = consumer;
  }

  @Override
  public Nodes render() {
    var ref = new Ref<MetaNode>();
    var para = Para.builder()
      .setString(supplier)
      .constantStyle(style -> style.setMaxLinesCount(1L))
      .setLine(true)
      .build();

    return EzNode.builder()
      .ref(ref)
      .listen(
        onFocus(event -> isFocused.accept(true)),
        onBlur(event -> isFocused.accept(false)),
        onMouseClick(event -> cursorPosition.accept(Optional.of(event.getPoint().getX())))
      )
//      .layout(EzLayout.builder()
//        .border(insets(2f))
//        .padding(insets(pixel(4f)))
//        .build()
//      )
//      .paint((canvas, layout) -> {
//        if (isFocused.get()) {
//          try (var paint = new Paint()) {
//            paint.setColor(EzColors.SKY_500);
//            float radius = 2f;
//            canvas.drawDRRect(layout.getBorderRect().withRadii(radius),
//              layout.getPaddingRect().withRadii(radius), paint);
//          }
//        }
//      })
      .paintAfter((canvas, layout) -> {
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
            paint.setStrokeWidth(1f);
            canvas.drawLine(x, rect.getTop(), x, rect.getBottom(), paint);
          }
        });
      })
      .children(para)
      .build();
  }
}
