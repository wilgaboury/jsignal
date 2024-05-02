package com.github.wilgaboury.jsigwig.examples;

import com.github.wilgaboury.jsignal.Constant;
import com.github.wilgaboury.sigui.*;
import com.github.wilgaboury.sigwig.InputLine;
import com.github.wilgaboury.sigwig.Para;
import com.github.wilgaboury.sigwig.ez.EzColors;
import com.github.wilgaboury.sigwig.ez.EzLayout;
import com.github.wilgaboury.sigwig.ez.EzNode;

@SiguiComponent
public class InputLineTest implements Renderable {
  public static void main(String[] args) {
    SiguiThread.start(() -> SiguiUtil.provideHotswapInstrumentation(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new SiguiWindow(window, InputLineTest::new);
    }));
  }

  @Override
  public Nodes render() {
    return EzNode.builder()
      .layout(EzLayout.builder()
        .fill()
        .center()
        .column()
        .gap(10f)
        .build()
      )
      .children(
        Para.style.constantCustomize(style -> style.setTextStyle(text -> text.setColor(EzColors.BLACK))).provide(() ->
          new InputLine(Constant.of("HELLO"), ignored -> {}).getNodes()
        )
      )
      .build();
  }
}

