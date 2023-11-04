package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.sigui.*;
import com.google.common.net.MediaType;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.SamplingMode;
import io.github.humbleui.skija.svg.SVGDOM;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;
import org.lwjgl.util.yoga.Yoga;

import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Image {
    private static final Logger logger = Logger.getLogger(Image.class.getName());

    private static final WeakHashMap<Blob, SVGDOM> SVG_DOMS = new WeakHashMap<>();
    private static final WeakHashMap<Blob, io.github.humbleui.skija.Image> IMAGE_OBJS = new WeakHashMap<>();

    private static SVGDOM getSvgDom(Blob blob) {
        return SVG_DOMS.computeIfAbsent(blob, b -> new SVGDOM(Data.makeFromBytes(b.getData())));
    }

    public static io.github.humbleui.skija.Image getImageObject(Blob blob) {
        if (IMAGE_OBJS.containsKey(blob)) {
            return IMAGE_OBJS.get(blob);
        } else {
            var img = io.github.humbleui.skija.Image.makeDeferredFromEncodedBytes(blob.getData());
            assert img != null : "could not parse image";
            IMAGE_OBJS.put(blob, img);
            return img;
        }
    }

    public static Node create(Builder builder) {
        Computed<Painter> painter = ReactiveUtil.createComputed(() -> painter(builder.blob, builder.fit));
        return Node.builder()
                .info(NodeInfo.builder()
                        .setId("image")
                        .build())
                .layout(yoga -> {
                    var blob = builder.blob.get();
                    if (blob.getMime().is(MediaType.SVG_UTF_8)) {
                        layoutVector(yoga, builder);
                    } else if (blob.getMime().is(MediaType.ANY_IMAGE_TYPE)) {
                        layoutRaster(yoga, builder);
                    } else {
                        logger.log(Level.WARNING, "Unrecognized image type: %s", blob.getMime());
                        Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
                        Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
                    }
                })
                .paint((canvas, yoga) -> painter.get().paint(canvas, yoga))
                .build();
    }

    public static void layoutVector(long yoga, Builder builder) {
        var blob = builder.blob.get();
        var userWidth = builder.width.get();
        var userHeight = builder.height.get();

        var svg = getSvgDom(blob);
        var dim = viewBox(svg);
        var imgWidth = dim.getX();
        var imgHeight = dim.getY();
        layoutImage(yoga, userWidth, userHeight, imgWidth, imgHeight);
    }

    public static void layoutRaster(long yoga, Builder builder) {
        var blob = builder.blob.get();
        var userWidth = builder.width.get();
        var userHeight = builder.height.get();
        var img = getImageObject(blob);
        var imgWidth = img.getWidth();
        var imgHeight = img.getHeight();
        layoutImage(yoga, userWidth, userHeight, imgWidth, imgHeight);
    }

    public static void layoutImage(
            long yoga,
            MaybePercent<Float> userWidth,
            MaybePercent<Float> userHeight,
            float imgWidth,
            float imgHeight
    ) {
        if (userWidth != null) {
            if (userWidth.isPercent()) {
                Yoga.YGNodeStyleSetWidthPercent(yoga, userWidth.value());
            } else {
                Yoga.YGNodeStyleSetWidth(yoga, userWidth.value());
            }
        }
        if (userHeight != null) {
            if (userHeight.isPercent()) {
                Yoga.YGNodeStyleSetHeightPercent(yoga, userHeight.value());
            } else {
                Yoga.YGNodeStyleSetHeight(yoga, userHeight.value());
            }
        }

        if (userWidth == null && userHeight != null) {
            Yoga.YGNodeSetMeasureFunc(yoga, (node, width, widthMode, height, heightMode, __result) -> {
                __result.width((imgWidth/imgHeight)*height);
                __result.height(height);
            });
        } else if (userHeight == null && userWidth != null) {
            Yoga.YGNodeSetMeasureFunc(yoga, (node, width, widthMode, height, heightMode, __result) -> {
                __result.width(width);
                __result.height((imgHeight/imgWidth)*width);
            });
        } else if (userWidth == null && userHeight == null) {
            Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
            Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
        }
    }

    private static Painter painter(Supplier<Blob> blobSupplier, Supplier<Fit> fitSupplier) {
        var blob = blobSupplier.get();
        var fit = fitSupplier.get();
        if (blob.getMime().is(MediaType.SVG_UTF_8)) {
            var svg = getSvgDom(blob);
            var viewBox = viewBox(svg);
            return (canvas, node) -> paintVector(canvas, node, svg, fit, viewBox.getX(), viewBox.getY());
        } else if (blob.getMime().is(MediaType.ANY_IMAGE_TYPE)) {
            var img = getImageObject(blob);
            return (canvas, node) -> paintRaster(canvas, node, img, fit);

        } else {
            logger.log(Level.WARNING, "Unrecognized image type: %s", blob.getMime());
            return (canvas, yoga) -> {};
        }
    }

    private static void paintVector(
            Canvas canvas,
            MetaNode node,
            SVGDOM svg,
            Fit fit,
            float imgHeight,
            float imgWidth
    ) {
        var size = node.getLayout().getSize();

        if (fit == Fit.FILL) {
            var svgRatio = imgWidth/imgHeight;
            var viewRatio = size.getX()/size.getY();

            if (viewRatio > svgRatio) {
                var scale = viewRatio/svgRatio;
                canvas.translate(size.getX()/2f, 0);
                canvas.scale(scale, 1f);
                canvas.translate(-size.getX()/2f, 0);
            } else if (viewRatio < svgRatio) {
                var scale = 1f/(viewRatio/svgRatio);
                canvas.translate(0, size.getY()/2f);
                canvas.scale(1f, scale);
                canvas.translate(0, -size.getY()/2f);
            }
        } else if (fit == Fit.COVER) {
            var svgRatio = imgWidth/imgHeight;
            var viewRatio = size.getX()/size.getY();

            canvas.clipRect(Rect.makeWH(size));

            if (viewRatio > svgRatio) {
                var scale = viewRatio/svgRatio;
                canvas.translate(size.getX()/2, size.getY()/2);
                canvas.scale(scale, scale);
                canvas.translate(-size.getX()/2, -size.getY()/2);
            } else if (viewRatio < svgRatio) {
                var scale = svgRatio/viewRatio;
                canvas.translate(size.getX()/2f, size.getY()/2f);
                canvas.scale(scale, scale);
                canvas.translate(-size.getX()/2f, -size.getY()/2f);
            }
        }

        svg.setContainerSize(size.getX(), size.getY());
        svg.render(canvas);
    }

    private static void paintRaster(
            Canvas canvas,
            MetaNode node,
            io.github.humbleui.skija.Image img,
            Fit fit
    ) {
        var size = node.getLayout().getSize();

        switch (fit) {
            case CONTAIN -> {
                var imgRatio = (float)img.getWidth()/(float)img.getHeight();
                var layoutRatio = size.getX()/size.getY();

                if (imgRatio > layoutRatio) {
                    var scale = size.getX()/(float)img.getWidth();
                    canvas.translate(0, size.getY()/2 - (scale*img.getHeight())/2);
                    drawImage(canvas, img, Rect.makeWH(size.getX(), scale*img.getHeight()));
                } else if (imgRatio < layoutRatio) {
                    var scale = size.getY()/(float)img.getHeight();
                    canvas.translate(size.getX()/2 - (scale*img.getWidth())/2, 0);
                    drawImage(canvas, img, Rect.makeWH(scale*img.getWidth(), size.getY()));
                } else {
                    drawImage(canvas, img, Rect.makeWH(size));
                }
            }
            case FILL -> {
                var imgRatio = (float)img.getWidth()/(float)img.getHeight();
                var layoutRatio = size.getX()/size.getY();

                canvas.clipRect(Rect.makeWH(size));

                if (imgRatio > layoutRatio) {
                    var scale = size.getY()/(float)img.getHeight();
                    canvas.translate(size.getX()/2 - (scale*img.getWidth())/2, 0);
                    drawImage(canvas, img, Rect.makeWH(scale*img.getWidth(), size.getY()));
                } else if (imgRatio < layoutRatio) {
                    var scale = size.getX()/(float)img.getWidth();
                    canvas.translate(0, size.getY()/2 - (scale*img.getHeight())/2);
                    drawImage(canvas, img, Rect.makeWH(size.getX(), scale*img.getHeight()));
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

    public enum Fit {
        CONTAIN,
        FILL,
        COVER
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Supplier<Blob> blob;
        private Supplier<Fit> fit = ReactiveUtil.constantSupplier(Fit.CONTAIN);
        private Supplier<MaybePercent<Float>> height = () -> null;
        private Supplier<MaybePercent<Float>> width = () -> null;

        public Builder blob(Supplier<Blob> blob) {
            this.blob = blob;
            return this;
        }

        public Builder blob(Blob blob) {
            this.blob = ReactiveUtil.constantSupplier(blob);
            return this;
        }

        public Builder fit(Supplier<Fit> fit) {
            this.fit = fit;
            return this;
        }

        public Builder fit(Fit fit) {
            this.fit = ReactiveUtil.constantSupplier(fit);
            return this;
        }

        public Builder height(Supplier<Float> height) {
            this.height = () -> new MaybePercent<>(false, height.get());
            return this;
        }

        public Builder height(float height) {
            this.height = () -> new MaybePercent<>(false, height);
            return this;
        }

        public Builder width(Supplier<Float> width) {
            this.width = () -> new MaybePercent<>(false, width.get());
            return this;
        }

        public Builder width(float width) {
            this.width = () -> new MaybePercent<>(false, width);
            return this;
        }

        public Builder heightPercent(Supplier<Float> height) {
            this.height = () -> new MaybePercent<>(true, height.get());
            return this;
        }

        public Builder heightPercent(float height) {
            this.height = () -> new MaybePercent<>(true, height);
            return this;
        }

        public Builder widthPercent(Supplier<Float> width) {
            this.width = () -> new MaybePercent<>(true, width.get());
            return this;
        }

        public Builder widthPercent(float width) {
            this.width = () -> new MaybePercent<>(true, width);
            return this;
        }

        public Node build() {
            return create(this);
        }
    }

    private static Point viewBox(SVGDOM svg) {
        if (svg.getRoot() == null) {
            logger.log(Level.SEVERE, "svg is missing root element");
            return null;
        }

        if (svg.getRoot().getViewBox() != null) {
            var box = svg.getRoot().getViewBox();
            return new Point(box.getWidth(), box.getHeight());
        } else {
            // TODO: handle missing viewbox
//            return new Point(svg.getRoot().getWidth().getValue(), svg.getRoot().getHeight().getValue());
            logger.log(Level.SEVERE, "svg missing viewbox attribute");
            return null;
        }
    }
}
