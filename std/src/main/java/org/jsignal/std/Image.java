package org.jsignal.std;

import com.google.common.net.MediaType;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.SamplingMode;
import io.github.humbleui.skija.svg.SVGDOM;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import jakarta.annotation.Nullable;
import org.jsignal.rx.Computed;
import org.jsignal.std.ez.EzNode;
import org.jsignal.ui.Element;
import org.jsignal.ui.Painter;
import org.jsignal.ui.Component;
import org.jsignal.ui.layout.Layout;
import org.jsignal.ui.layout.LayoutConfig;
import org.jsignal.ui.layout.LayoutValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import static org.jsignal.ui.layout.LayoutValue.percent;

public class Image extends Component {
  private final static Logger logger = LoggerFactory.getLogger(Image.class);

  private static final WeakHashMap<Blob, SVGDOM> svgDoms = new WeakHashMap<>();
  private static final WeakHashMap<Blob, io.github.humbleui.skija.Image> imageObjects = new WeakHashMap<>();

  private static SVGDOM getSvgDom(Blob blob) {
    return svgDoms.computeIfAbsent(blob, b -> new SVGDOM(Data.makeFromBytes(b.data())));
  }

  private static io.github.humbleui.skija.Image getImageObject(Blob blob) {
    return imageObjects.computeIfAbsent(blob, b ->
      io.github.humbleui.skija.Image.makeDeferredFromEncodedBytes(blob.data()));
  }

  Supplier<Blob> blob;
  Supplier<Fit> fit;
  Supplier<Optional<LayoutValue>> width;
  Supplier<Optional<LayoutValue>> height;

  private final Computed<Painter> painter;

  public Image(Builder builder) {
    this.blob = builder.blob;
    this.fit = builder.fit;
    this.width = builder.width;
    this.height = builder.height;

    this.painter = Computed.create(() -> createPainter(blob.get(), fit.get()));
  }

  @Override
  public Element render() {
    return EzNode.builder()
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
    var imgWidth = dim.getX();
    var imgHeight = dim.getY();
    layoutImage(config, imgWidth, imgHeight,
      width.get().orElse(null), height.get().orElse(null)
    );
  }

  private void layoutRaster(LayoutConfig config) {
    var img = getImageObject(blob.get());
    var imgWidth = img.getWidth();
    var imgHeight = img.getHeight();
    layoutImage(config, imgWidth, imgHeight,
      width.get().orElse(null), height.get().orElse(null)
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
      var viewBox = viewBox(svg);
      return (canvas, node) -> paintVector(canvas, node, svg, fit, viewBox.getX(), viewBox.getY());
    } else if (blob.mime().is(MediaType.ANY_IMAGE_TYPE)) {
      var img = getImageObject(blob);
      return (canvas, layout) -> paintRaster(canvas, layout, img, fit);
    } else {
      logger.warn("Unrecognized image type: {}", blob.mime());
      return (canvas, yoga) -> {
      };
    }
  }

  private static void paintVector(
    Canvas canvas,
    Layout layout,
    SVGDOM svg,
    Fit fit,
    float imgHeight,
    float imgWidth
  ) {
    var size = layout.getSize();

    if (fit == Fit.FILL) {
      var svgRatio = imgWidth / imgHeight;
      var viewRatio = size.getX() / size.getY();

      if (viewRatio > svgRatio) {
        var scale = viewRatio / svgRatio;
        canvas.translate(size.getX() / 2f, 0);
        canvas.scale(scale, 1f);
        canvas.translate(-size.getX() / 2f, 0);
      } else if (viewRatio < svgRatio) {
        var scale = 1f / (viewRatio / svgRatio);
        canvas.translate(0, size.getY() / 2f);
        canvas.scale(1f, scale);
        canvas.translate(0, -size.getY() / 2f);
      }
    } else if (fit == Fit.COVER) {
      var svgRatio = imgWidth / imgHeight;
      var viewRatio = size.getX() / size.getY();

      canvas.clipRect(Rect.makeWH(size));

      if (viewRatio > svgRatio) {
        var scale = viewRatio / svgRatio;
        canvas.translate(size.getX() / 2, size.getY() / 2);
        canvas.scale(scale, scale);
        canvas.translate(-size.getX() / 2, -size.getY() / 2);
      } else if (viewRatio < svgRatio) {
        var scale = svgRatio / viewRatio;
        canvas.translate(size.getX() / 2f, size.getY() / 2f);
        canvas.scale(scale, scale);
        canvas.translate(-size.getX() / 2f, -size.getY() / 2f);
      }
    }

    svg.setContainerSize(size.getX(), size.getY());
    svg.render(canvas);
  }

  private static void paintRaster(
    Canvas canvas,
    Layout layout,
    io.github.humbleui.skija.Image img,
    Fit fit
  ) {
    var size = layout.getSize();

    switch (fit) {
      case CONTAIN -> {
        var imgRatio = (float) img.getWidth() / (float) img.getHeight();
        var layoutRatio = size.getX() / size.getY();

        if (imgRatio > layoutRatio) {
          var scale = size.getX() / (float) img.getWidth();
          canvas.translate(0, size.getY() / 2 - (scale * img.getHeight()) / 2);
          drawImage(canvas, img, Rect.makeWH(size.getX(), scale * img.getHeight()));
        } else if (imgRatio < layoutRatio) {
          var scale = size.getY() / (float) img.getHeight();
          canvas.translate(size.getX() / 2 - (scale * img.getWidth()) / 2, 0);
          drawImage(canvas, img, Rect.makeWH(scale * img.getWidth(), size.getY()));
        } else {
          drawImage(canvas, img, Rect.makeWH(size));
        }
      }
      case FILL -> {
        var imgRatio = (float) img.getWidth() / (float) img.getHeight();
        var layoutRatio = size.getX() / size.getY();

        canvas.clipRect(Rect.makeWH(size));

        if (imgRatio > layoutRatio) {
          var scale = size.getY() / (float) img.getHeight();
          canvas.translate(size.getX() / 2 - (scale * img.getWidth()) / 2, 0);
          drawImage(canvas, img, Rect.makeWH(scale * img.getWidth(), size.getY()));
        } else if (imgRatio < layoutRatio) {
          var scale = size.getX() / (float) img.getWidth();
          canvas.translate(0, size.getY() / 2 - (scale * img.getHeight()) / 2);
          drawImage(canvas, img, Rect.makeWH(size.getX(), scale * img.getHeight()));
        } else {
          drawImage(canvas, img, Rect.makeWH(size));
        }
      }
      case COVER -> drawImage(canvas, img, Rect.makeWH(size));
    }
  }

  private static void drawImage(Canvas canvas, io.github.humbleui.skija.Image img, Rect rect) {
    canvas.drawImageRect(
      img,
      Rect.makeWH(img.getWidth(), img.getHeight()),
      Rect.makeWH(rect.getWidth(), rect.getHeight()),
      SamplingMode.LINEAR,
      null,
      true
    );
  }


  private static Point viewBox(SVGDOM svg) {
    if (svg.getRoot() == null) {
      logger.error("svg is missing root element");
      return null;
    }

    if (svg.getRoot().getViewBox() != null) {
      var box = svg.getRoot().getViewBox();
      return new Point(box.getWidth(), box.getHeight());
    } else {
      // TODO: handle missing viewbox
//            return new Point(svg.getRoot().getWidth().getValue(), svg.getRoot().getHeight().getValue());
      logger.error("svg missing viewbox attribute");
      return null;
    }
  }

  public enum Fit {
    CONTAIN,
    FILL,
    COVER
  }

  public static BuilderSetBlob builder() {
    return new Builder();
  }

  public interface BuilderSetBlob {
    BuilderSetWidthOrHeight setBlob(Blob blob);

    BuilderSetWidthOrHeight setBlob(Supplier<Blob> blob);
  }

  public interface BuilderSetWidthOrHeight {
    Builder setHeight(LayoutValue height);

    Builder setHeight(Supplier<LayoutValue> height);

    Builder setWidth(LayoutValue width);

    Builder setWidth(Supplier<LayoutValue> width);
  }

  public static class Builder implements BuilderSetBlob, BuilderSetWidthOrHeight {
    private Supplier<Blob> blob;
    private Supplier<Optional<LayoutValue>> height = Optional::empty;
    private Supplier<Optional<LayoutValue>> width = Optional::empty;
    private Supplier<Fit> fit = () -> Fit.CONTAIN;

    public Supplier<Blob> getBlob() {
      return blob;
    }

    @Override
    public BuilderSetWidthOrHeight setBlob(Blob blob) {
      return setBlob(() -> blob);
    }

    @Override
    public BuilderSetWidthOrHeight setBlob(Supplier<Blob> blob) {
      this.blob = blob;
      return this;
    }

    public Supplier<Optional<LayoutValue>> getHeight() {
      return height;
    }

    public Builder setHeight(LayoutValue height) {
      return setHeight(() -> height);
    }

    public Builder setHeight(Supplier<LayoutValue> height) {
      this.height = () -> Optional.ofNullable(height.get());
      return this;
    }

    public Supplier<Optional<LayoutValue>> getWidth() {
      return width;
    }

    public Builder setWidth(LayoutValue width) {
      return setWidth(() -> width);
    }

    public Builder setWidth(Supplier<LayoutValue> width) {
      this.width = () -> Optional.ofNullable(width.get());
      return this;
    }

    public Supplier<Fit> getFit() {
      return fit;
    }

    public Builder setFit(Fit fit) {
      return setFit(() -> fit);
    }

    public Builder setFit(Supplier<Fit> fit) {
      this.fit = fit;
      return this;
    }

    public Image build() {
      return new Image(this);
    }
  }
}