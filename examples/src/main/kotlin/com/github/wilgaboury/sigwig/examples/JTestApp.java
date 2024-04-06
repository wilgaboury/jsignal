package com.github.wilgaboury.sigwig.examples;

import com.github.wilgaboury.jsigwig.*;
import com.github.wilgaboury.jsigwig.text.TextLine;
import com.github.wilgaboury.sigui.*;
import com.google.common.net.MediaType;

public class JTestApp extends Component {
  public static void main(String[] args) {
    SiguiUtil.start(() -> {
      var window = SiguiUtil.createWindow();
      window.setTitle("Test App");
      window.setContentSize(400, 400);
      new SiguiWindow(window, JTestApp::new);
    });
  }

  public static final Blob fireSvg;
  static {
    try {
      fireSvg = Blob.fromResource("/fire.svg", MediaType.SVG_UTF_8);
    } catch (BlobException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Nodes render() {
    return Node.builder()
      .layout(Flex.builder()
        .stretch()
        .center()
        .gap(20f)
        .build()
      )
      .children(
        TextLine.builder()
          .setLine(InterFontUtil.createTextLine("Hello World!", 16f))
          .setColor(EzColors.BLUE_800)
          .build(),
        Image.builder()
          .setBlob(fireSvg)
          .setWidth(LayoutValue.pixel(100))
          .build()
      )
      .build();
  }
}
