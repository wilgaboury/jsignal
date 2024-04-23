package com.github.wilgaboury.sigwig.text;

import com.github.wilgaboury.jsignal.Constant;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.SiguiComponent;
import com.github.wilgaboury.sigui.layout.LayoutConfig;
import io.github.humbleui.skija.paragraph.Paragraph;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.JSignalUtil.maybeComputed;

@SiguiComponent
public class Para implements Renderable {
  private final Supplier<Paragraph> para;

  public Para(Paragraph para) {
    this(Constant.of(para));
  }

  public Para(Supplier<Paragraph> para) {
    this.para = maybeComputed(para);
  }

  @Override
  public Nodes render() {
    return Node.builder()
      .layout(config -> {
        config.setMaxHeightPercent(100f);
        config.setMeasure((width, widthMode, height, heightMode) -> {
          var p = para.get();
          p.layout(width);
          config.setMinWidth(p.getMinIntrinsicWidth());
          return new LayoutConfig.Size(p.getMaxIntrinsicWidth(), p.getHeight());
        });
      })
      .paint((canvas, node) -> {
        var p = para.get();
        p.paint(canvas, 0f, 0f);
      })
      .build();
  }
}
