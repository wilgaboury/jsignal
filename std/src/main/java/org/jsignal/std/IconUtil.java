package org.jsignal.std;

import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.svg.SVGDOM;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Standard component library uses <a href="https://remixicon.com/">https://remixicon.com/</a> for all icons
 */
public class IconUtil {
  public static SVGDOM fromStream(@Nullable InputStream inputStream) {
    if (inputStream == null) {
      throw new RuntimeException("stream was null");
    }

    try {
      try {
        var result = inputStream.readAllBytes();
        return new SVGDOM(Data.makeFromBytes(result));
      } finally {
        inputStream.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
