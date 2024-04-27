package com.github.wilgaboury.sigwig.text;

import com.github.wilgaboury.jsignal.Constant;
import com.github.wilgaboury.sigui.Nodes;
import com.github.wilgaboury.sigui.Renderable;
import com.github.wilgaboury.sigui.SiguiComponent;
import com.github.wilgaboury.sigui.layout.LayoutConfig;
import com.github.wilgaboury.sigwig.ez.EzNode;
import io.github.humbleui.skija.paragraph.Paragraph;

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
    return EzNode.builder()
      .layout(config -> {
        var p = para.get();
        config.setMeasure((width, widthMode, height, heightMode) -> {
          p.layout(width);
          return new LayoutConfig.Size(width, p.getHeight());
        });
      })
      .paint((canvas, node) -> para.get().paint(canvas, 0f, 0f))
      .build();
  }
}
