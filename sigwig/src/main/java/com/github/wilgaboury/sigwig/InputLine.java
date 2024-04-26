package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.jsignal.Signal;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.text.TextLine;
import io.github.humbleui.skija.Paint;

import static com.github.wilgaboury.sigui.event.EventListener.*;
import static com.github.wilgaboury.sigui.layout.Insets.insets;
import static com.github.wilgaboury.sigui.layout.LayoutValue.pixel;

@SiguiComponent
public class InputLine implements Renderable {
  private final Signal<Integer> cursorPosition = Signal.create(0);
  private final Signal<Boolean> cursorShow = Signal.create(false);
  private final Signal<Boolean> isFocused = Signal.create(false);
  private io.github.humbleui.skija.TextLine line;

  @Override
  public Nodes render() {
    var ref = new Ref<MetaNode>();
    return Node.builder()
      .ref(ref)
      .listen(
        onFocus(event -> isFocused.accept(true)),
        onBlur(event -> isFocused.accept(false)),
        onMouseClick(event -> {
          cursorPosition.accept(line.getOffsetAtCoord(event.getPoint().getX()));
        })
      )
      .layout(EzLayout.builder()
        .border(insets(2f))
        .padding(insets(pixel(4f)))
        .build()
      )
      .paint((canvas, layout) -> {
        if (isFocused.get()) {
          try (var paint = new Paint()) {
            paint.setColor(EzColors.SKY_500);
            float radius = 2f;
            canvas.drawDRRect(layout.getBorderRect().withRadii(radius),
              layout.getPaddingRect().withRadii(radius), paint);
          }
        }
      })
      .children(
        TextLine.builder()
          .setLine(line)
          .setColor(EzColors.BLACK)
          .build()
      )
      .build();
  }
}
