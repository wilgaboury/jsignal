package org.jsignal.std;

import com.google.common.net.MediaType;
import jakarta.annotation.Nullable;
import org.apache.batik.gvt.GraphicsNode;

import java.io.IOException;
import java.io.InputStream;

/**
 * Standard component library uses <a href="https://remixicon.com/">https://remixicon.com/</a> for all icons
 */
public class IconUtil {
  public static Svg fromStream(@Nullable InputStream inputStream) {
    if (inputStream == null) {
      throw new RuntimeException("stream was null");
    }

    try {
      try {
        var result = inputStream.readAllBytes();
        return Svg.fromBlob(new Blob(result, MediaType.SVG_UTF_8));
      } finally {
        inputStream.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
