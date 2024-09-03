package org.jsignal.examples;

import io.github.humbleui.skija.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterFontUtil {
  private static final Logger logger = LoggerFactory.getLogger(InterFontUtil.class);

  public static io.github.humbleui.skija.TextLine createTextLine(String string, float size) {
    var font = new Font();
    font.setSize(size);
    return io.github.humbleui.skija.TextLine.make(string, font);
  }
}
