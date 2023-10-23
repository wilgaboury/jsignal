package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.YogaUtil;
import com.google.common.net.MediaType;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.svg.SVGDOM;
import io.github.humbleui.types.Point;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Image {
    private static final Logger logger = Logger.getLogger(Image.class.getName());

    public static Node create(Supplier<Blob> blob, Supplier<Fit> fit) {
        Computed<Node.Painter> painter = ReactiveUtil.createComputed(() -> painter(blob, fit));
        return Node.builder()
                .setLayout(Flex.builder().stretch().build())
                .setPaint((canvas, yoga) -> painter.get().paint(canvas, yoga))
                .build();
    }

    private static Node.Painter painter(Supplier<Blob> blobSupplier, Supplier<Fit> fitSupplier) {
        var blob = blobSupplier.get();
        var fit = fitSupplier.get();
        if (blob.getMime().equals(MediaType.SVG_UTF_8)) {
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

        public Node build() {
            return create(blob, fit);
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
