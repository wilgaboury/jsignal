package org.jsignal.std;

import com.google.common.net.MediaType;
import jakarta.annotation.Nullable;
import org.apache.batik.gvt.GraphicsNode;
import org.joml.Vector2f;
import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.Painter;
import org.jsignal.ui.layout.Layout;
import org.jsignal.ui.layout.LayoutConfig;
import org.jsignal.ui.layout.LayoutValue;
import org.jsignal.ui.layout.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.createMemo;
import static org.jsignal.ui.layout.LayoutValue.percent;

@GeneratePropComponent
public non-sealed class Image extends ImagePropComponent {
  private final static Logger logger = LoggerFactory.getLogger(Image.class);

  private static final WeakHashMap<Blob, Svg> svgDoms = new WeakHashMap<>();
  private static final WeakHashMap<Blob, BufferedImage> imageObjects = new WeakHashMap<>();

  private static Svg getSvgDom(Blob blob) {
    return svgDoms.computeIfAbsent(blob, Svg::fromBlob);
  }

  private static BufferedImage getImageObject(Blob blob) {
    return imageObjects.computeIfAbsent(blob, b -> {
      try {
        return ImageIO.read(new ByteArrayInputStream(blob.data()));
      } catch (IOException e) {
        throw new RuntimeException("Failed to read image", e);
      }
    });
  }

  @Prop(required = true)
  Supplier<Blob> blob;
  @Prop(oneofKey = "dim")
  Supplier<LayoutValue> width = Constant.of(null);
  @Prop(oneofKey = "dim")
  Supplier<LayoutValue> height = Constant.of(null);
  @Prop
  Supplier<Fit> fit = Constant.of(Fit.CONTAIN);

  @Override
  public Element render() {
    var painter = createMemo(() -> createPainter(blob.get(), fit.get()));

    return Node.builder()
      .layout(config -> {
        if (blob.get().mime().is(MediaType.SVG_UTF_8)) {
          layoutVector(config);
        } else if (blob.get().mime().is(MediaType.ANY_IMAGE_TYPE)) {
          layoutRaster(config);
        } else {
          logger.warn("Unrecognized image type: {}", blob.get().mime());
          config.setWidth(percent(100f));
          config.setHeight(percent(100f));
        }
      })
      .paint((canvas, yoga) -> painter.get().paint(canvas, yoga))
      .build();
  }

  private void layoutVector(LayoutConfig config) {
    var svg = getSvgDom(blob.get());
    var dim = viewBox(svg);
    var imgWidth = dim.x();
    var imgHeight = dim.y();
    layoutImage(config, imgWidth, imgHeight,
      width.get(), height.get()
    );
  }

  private void layoutRaster(LayoutConfig config) {
    var img = getImageObject(blob.get());
    var imgWidth = img.getWidth();
    var imgHeight = img.getHeight();
    layoutImage(config, imgWidth, imgHeight,
      width.get(), height.get()
    );
  }

  private void layoutImage(
    LayoutConfig config,
    float imgWidth,
    float imgHeight,
    @Nullable LayoutValue userWidth,
    @Nullable LayoutValue userHeight
  ) {
    if (userWidth != null) {
      config.setWidth(userWidth);
    }
    if (userHeight != null) {
      config.setHeight(userHeight);
    }
    if (userWidth == null && userHeight != null) {
      config.setMeasure((width, widthMode, height, heightMode) ->
        new LayoutConfig.Size((imgWidth / imgHeight) * height, height)
      );
    } else if (userHeight == null && userWidth != null) {
      config.setMeasure((width, widthMode, height, heightMode) ->
        new LayoutConfig.Size(width, (imgHeight / imgWidth) * width));
    } else if (userWidth == null && userHeight == null) {
      config.setWidth(percent(100f));
      config.setHeight(percent(100f));
    }
  }

  private static Painter createPainter(Blob blob, Fit fit) {
    if (blob.mime().is(MediaType.SVG_UTF_8)) {
      var svg = getSvgDom(blob);
      return (canvas, node) -> paintSvg(canvas, node, svg, fit);
    } else if (blob.mime().is(MediaType.ANY_IMAGE_TYPE)) {
      var img = getImageObject(blob);
      return (canvas, layout) -> paintRaster(canvas, layout, img, fit);
    } else {
      logger.warn("Unrecognized image type: {}", blob.mime());
      return (canvas, yoga) -> {};
    }
  }

  public static void paintSvg(
    Graphics2D g2d,
    Layout layout,
    Svg svg,
    Fit fit
  ) {
    var viewBox = viewBox(svg);
    paintSvg(g2d, layout, svg, fit, viewBox.x(), viewBox.y());
  }

  public static void paintSvg(
    Graphics2D g2d,
    Layout layout,
    Svg svg,
    Fit fit,
    float imgHeight,
    float imgWidth
  ) {
    var size = layout.getSize();

    if (fit == Fit.FILL) {
      var svgRatio = imgWidth / imgHeight;
      var viewRatio = size.x() / size.y();

      if (viewRatio > svgRatio) {
        var scale = viewRatio / svgRatio;
        g2d.translate(size.x() / 2f, 0);
        g2d.scale(scale, 1f);
        g2d.translate(-size.x() / 2f, 0);
      } else if (viewRatio < svgRatio) {
        var scale = 1f / (viewRatio / svgRatio);
        g2d.translate(0, size.y() / 2f);
        g2d.scale(1f, scale);
        g2d.translate(0, -size.y() / 2f);
      }
    } else if (fit == Fit.COVER) {
      var svgRatio = imgWidth / imgHeight;
      var viewRatio = size.x() / size.y();

      g2d.clipRect(0, 0, (int)Math.ceil(size.x()), (int)Math.ceil(size.y()));

      if (viewRatio > svgRatio) {
        var scale = viewRatio / svgRatio;
        g2d.translate(size.x() / 2, size.y() / 2);
        g2d.scale(scale, scale);
        g2d.translate(-size.x() / 2, -size.y() / 2);
      } else if (viewRatio < svgRatio) {
        var scale = svgRatio / viewRatio;
        g2d.translate(size.x() / 2f, size.y() / 2f);
        g2d.scale(scale, scale);
        g2d.translate(-size.x() / 2f, -size.y() / 2f);
      }
    }

    float sx = imgWidth / (float)svg.graphics().getBounds().getWidth();
    float sy = imgHeight / (float)svg.graphics().getBounds().getHeight();
    g2d.scale(sx, sy);
    svg.graphics().paint(g2d);
  }

  public static void paintRaster(
    Graphics2D g2d,
    Layout layout,
    BufferedImage img,
    Fit fit
  ) {
    var size = layout.getSize();

    switch (fit) {
      case CONTAIN -> {
        var imgRatio = (float) img.getWidth() / (float) img.getHeight();
        var layoutRatio = size.x() / size.y();

        if (imgRatio > layoutRatio) {
          var scale = size.x() / (float) img.getWidth();
          g2d.translate(0, size.y() / 2 - (scale * img.getHeight()) / 2);
          drawImage(g2d, img, Rect.makeWH(size.x(), scale * img.getHeight()));
        } else if (imgRatio < layoutRatio) {
          var scale = size.y() / (float) img.getHeight();
          g2d.translate(size.x() / 2 - (scale * img.getWidth()) / 2, 0);
          drawImage(g2d, img, Rect.makeWH(scale * img.getWidth(), size.y()));
        } else {
          drawImage(g2d, img, Rect.makeWH(size));
        }
      }
      case FILL -> {
        var imgRatio = (float) img.getWidth() / (float) img.getHeight();
        var layoutRatio = size.x() / size.y();

        g2d.clipRect(0, 0, (int)Math.ceil(size.x()), (int)Math.ceil(size.y()));

        if (imgRatio > layoutRatio) {
          var scale = size.y() / (float) img.getHeight();
          g2d.translate(size.x() / 2 - (scale * img.getWidth()) / 2, 0);
          drawImage(g2d, img, Rect.makeWH(scale * img.getWidth(), size.y()));
        } else if (imgRatio < layoutRatio) {
          var scale = size.x() / (float) img.getWidth();
          g2d.translate(0, size.y() / 2 - (scale * img.getHeight()) / 2);
          drawImage(g2d, img, Rect.makeWH(size.x(), scale * img.getHeight()));
        } else {
          drawImage(g2d, img, Rect.makeWH(size));
        }
      }
      case COVER -> drawImage(g2d, img, Rect.makeWH(size));
    }
  }

  private static void drawImage(Graphics2D g2d, BufferedImage img, Rect rect) {
    g2d.scale(rect.getWidth()/(float)img.getWidth(), rect.getHeight()/(float)img.getHeight());
    g2d.drawImage(img, 0, 0, null);
  }


  private static Vector2f viewBox(Svg svg) {
    return new Vector2f((float)svg.graphics().getBounds().getWidth(), (float)svg.graphics().getBounds().getHeight());
  }

  public enum Fit {
    CONTAIN,
    FILL,
    COVER
  }
}