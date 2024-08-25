package com.github.wilgaboury.sigwig.text;

import com.github.wilgaboury.jsignal.JSignalUtil;
import com.github.wilgaboury.sigui.NodesSupplier;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.SiguiComponent;
import com.github.wilgaboury.sigwig.ez.EzNode;
import io.github.humbleui.skija.Paint;

import java.util.function.Supplier;

import static com.github.wilgaboury.sigui.layout.LayoutValue.pixel;

@SiguiComponent
public class TextLine implements Renderable {
  private final Supplier<io.github.humbleui.skija.TextLine> line;
  private final Supplier<Integer> color;

  public TextLine(Builder builder) {
    this.line = JSignalUtil.createMemo(builder.line);
    this.color = builder.color;
  }

  @Override
  public NodesSupplier render() {
    return EzNode.builder()
      .layout(config -> {
        var tmp = line.get();
        config.setWidth(pixel(tmp.getWidth()));
        config.setHeight(pixel(tmp.getHeight()));
      })
      .paint((canvas, node) -> {
        try (var paint = new Paint()) {
          var tmp = line.get();
          paint.setColor(color.get());
          canvas.drawTextLine(tmp, 0f, -tmp.getAscent(), paint);
        }
      })
      .build();
  }

  public static BuilderSetLine builder() {
    return new Builder();
  }

  public static class Builder implements BuilderSetLine, BuilderSetColor {
    private Supplier<io.github.humbleui.skija.TextLine> line;
    private Supplier<Integer> color;

    public Supplier<io.github.humbleui.skija.TextLine> getLine() {
      return line;
    }

    @Override
    public BuilderSetColor setLine(io.github.humbleui.skija.TextLine line) {
      return setLine(() -> line);
    }

    @Override
    public BuilderSetColor setLine(Supplier<io.github.humbleui.skija.TextLine> line) {
      this.line = line;
      return this;
    }

    public Supplier<Integer> getColor() {
      return color;
    }

    @Override
    public Builder setColor(Integer color) {
      return setColor(() -> color);
    }

    @Override
    public Builder setColor(Supplier<Integer> color) {
      this.color = color;
      return this;
    }

    public TextLine build() {
      return new TextLine(this);
    }
  }

  public interface BuilderSetLine {
    BuilderSetColor setLine(io.github.humbleui.skija.TextLine line);

    BuilderSetColor setLine(Supplier<io.github.humbleui.skija.TextLine> line);
  }

  public interface BuilderSetColor {
    Builder setColor(Integer color);

    Builder setColor(Supplier<Integer> color);
  }
}
