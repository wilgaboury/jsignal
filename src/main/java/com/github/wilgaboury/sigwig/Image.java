package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.jsignal.Computed;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.YogaUtil;
import com.google.common.net.MediaType;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.svg.SVGDOM;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Image {
    private static final Logger logger = Logger.getLogger(Image.class.getName());

    public static Node create(Supplier<Blob> blob) {
        Computed<Node.Painter> painter = ReactiveUtil.createComputed(() -> createPainter(blob));
        return Node.builder()
                .setLayout(Flex.builder().stretch().build())
                .setPaint((canvas, yoga) -> painter.get().paint(canvas, yoga))
                .build();
    }

    private static Node.Painter createPainter(Supplier<Blob> blobSupplier) {
        Blob blob = blobSupplier.get();
        if (blob.getMime().equals(MediaType.SVG_UTF_8)) {
            var svg = new SVGDOM(Data.makeFromBytes(blob.getData()));
            return (canvas, yoga) -> {
                var size = YogaUtil.boundingRect(yoga);
                svg.setContainerSize(size.getWidth(), size.getHeight());
                svg.render(canvas);
            };
        }

        logger.log(Level.WARNING, "Unrecognized image type: %s", blob.getMime());
        return (canvas, yoga) -> {};
    }
}
