package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.Painter;
import com.github.wilgaboury.sigui.YogaUtil;
import com.google.common.net.MediaType;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.svg.SVGDOM;
import io.github.humbleui.types.Point;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Image {
    private static final Logger logger = Logger.getLogger(Image.class.getName());

    public static Node create(Builder builder) {
        Computed<Painter> painter = ReactiveUtil.createComputed(() -> painter(builder.blob, builder.fit));
        return Node.builder()
                .layout(yoga -> {
                    var blob = builder.blob.get();
                    if (blob.getMime().is(MediaType.SVG_UTF_8)) {
                        layoutSvg(yoga, builder);
                    }
                })
                .paint((canvas, yoga) -> painter.get().paint(canvas, yoga))
                .build();
    }

    public static void layoutSvg(long yoga, Builder builder) {
        var blob = builder.blob.get();
        var imgWidth = builder.width.get();
        var imgHeight = builder.height.get();

        var svg = new SVGDOM(Data.makeFromBytes(blob.getData()));
        var dim = viewBox(svg);
        var svgWidth = dim.getX();
        var svgHeight = dim.getY();

        if (imgWidth != null) {
            if (imgWidth.isPercent()) {
                Yoga.YGNodeStyleSetWidthPercent(yoga, imgWidth.value());
            } else {
                Yoga.YGNodeStyleSetWidth(yoga, imgWidth.value());
            }
        }
        if (imgHeight != null) {
            if (imgHeight.isPercent()) {
                Yoga.YGNodeStyleSetHeightPercent(yoga, imgHeight.value());
            } else {
                Yoga.YGNodeStyleSetHeight(yoga, imgHeight.value());
            }
        }

        if (imgWidth == null && imgHeight != null) {
            Yoga.YGNodeSetMeasureFunc(yoga, (node, width, widthMode, height, heightMode, __result) -> {
                __result.width((svgWidth/svgHeight)*height);
                __result.height(height);
            });
        } else if (imgHeight == null && imgWidth != null) {
            Yoga.YGNodeSetMeasureFunc(yoga, (node, width, widthMode, height, heightMode, __result) -> {
                __result.width(width);
                __result.height((svgHeight/svgWidth)*width);
            });
        } else if (imgWidth == null && imgHeight == null) {
            Yoga.YGNodeStyleSetWidthPercent(yoga, 100f);
            Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
        }
    }

    private static Painter painter(Supplier<Blob> blobSupplier, Supplier<Fit> fitSupplier) {
        var blob = blobSupplier.get();
        var fit = fitSupplier.get();
        if (blob.getMime().is(MediaType.SVG_UTF_8)) {
            var svg = new SVGDOM(Data.makeFromBytes(blob.getData()));
            var dim = viewBox(svg);
            var width = dim.getX();
            var height = dim.getY();
            return (canvas, yoga) -> {
                var size = YogaUtil.boundingRect(yoga);

                if (fit == Fit.FILL) {
                    var svgRatio = width/height;
                    var viewRatio = size.getWidth()/size.getHeight();

                    if (viewRatio > svgRatio) {
                        var scale = viewRatio/svgRatio;
                        canvas.translate(size.getWidth()/2f, 0);
                        canvas.scale(scale, 1f);
                        canvas.translate(-size.getWidth()/2f, 0);
                    } else if (viewRatio < svgRatio) {
                        var scale = 1f/(viewRatio/svgRatio);
                        canvas.translate(0, size.getHeight()/2f);
                        canvas.scale(1f, scale);
                        canvas.translate(0, -size.getHeight()/2f);
                    }
                } else if (fit == Fit.COVER) {
                    var svgRatio = width/height;
                    var viewRatio = size.getWidth()/size.getHeight();

                    canvas.clipRect(size);

                    if (viewRatio > svgRatio) {
                        var scale = viewRatio/svgRatio;
                        canvas.translate(size.getWidth()/2, size.getHeight()/2);
                        canvas.scale(scale, scale);
                        canvas.translate(-size.getWidth()/2, -size.getHeight()/2);
                    } else if (viewRatio < svgRatio) {
                        var scale = 1f/(viewRatio/svgRatio);
                        canvas.translate(size.getWidth()/2f, size.getHeight()/2f);
                        canvas.scale(scale, scale);
                        canvas.translate(-size.getWidth()/2f, -size.getHeight()/2f);
                    }
                }

                svg.setContainerSize(size.getWidth(), size.getHeight());
                svg.render(canvas);
            };
        } else if (blob.getMime().is(MediaType.ANY_IMAGE_TYPE)) {
            blob.getData();
            var img = io.github.humbleui.skija.Image.makeDeferredFromEncodedBytes(blob.getData());
            return (canvas, yoga) -> {
                canvas.drawImage(img, 0, 0);
            };

        }
        logger.log(Level.WARNING, "Unrecognized image type: %s", blob.getMime());
        return (canvas, yoga) -> {};
    }

    public enum Fit {
        FILL,
        CONTAIN,
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

        public Builder setBlob(Supplier<Blob> blob) {
            this.blob = blob;
            return this;
        }

        public Builder setBlob(Blob blob) {
            this.blob = ReactiveUtil.constantSupplier(blob);
            return this;
        }

        public Builder setFit(Supplier<Fit> fit) {
            this.fit = fit;
            return this;
        }

        public Builder setFit(Fit fit) {
            this.fit = ReactiveUtil.constantSupplier(fit);
            return this;
        }

        public Builder height(Supplier<Float> height) {
            this.height = () -> new MaybePercent<>(false, height.get());
            return this;
        }

        public Builder width(Supplier<Float> width) {
            this.width = () -> new MaybePercent<>(false, width.get());
            return this;
        }

        public Builder heightPercent(Supplier<Float> height) {
            this.height = () -> new MaybePercent<>(true, height.get());
            return this;
        }

        public Builder widthPercent(Supplier<Float> width) {
            this.width = () -> new MaybePercent<>(true, width.get());
            return this;
        }

        public Node build() {
            return create(this);
        }
    }

    private static  Point viewBox(SVGDOM svg) {
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
