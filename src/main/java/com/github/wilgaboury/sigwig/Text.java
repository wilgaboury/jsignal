package com.github.wilgaboury.sigwig;

import com.github.wilgaboury.sigui.Node;
import io.github.humbleui.skija.*;
import io.github.humbleui.skija.paragraph.*;
import org.lwjgl.util.yoga.Yoga;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.wilgaboury.jsignal.ReactiveUtil.constantSupplier;

public class Text {
    private static final Logger logger = Logger.getLogger(Text.class.getName());

    public static Typeface INTER_REGULAR;
    public static Typeface INTER_BOLD;

    private static final String[] INTER_RESOURCE_LOCATIONS = new String[]{
            "/fonts/Inter-Bold.ttf",
            "/fonts/Inter-Italic.ttf",
            "/fonts/Inter-Regular.ttf"
    };
    public static final TypefaceFontProvider INTER_FONT_MGR;
    public static final FontCollection FONT_COLLECTION;
    static {
        INTER_FONT_MGR = new TypefaceFontProvider();
        try {
            for (var loc : INTER_RESOURCE_LOCATIONS) {
                try (var resource = Text.class.getResourceAsStream(loc)) {
                    if (resource != null) {
                        INTER_FONT_MGR.registerTypeface(Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes())));
                    }
                }
            }

            try (var resource = Text.class.getResourceAsStream("/fonts/Inter-Regular.ttf")) {
                if (resource != null) {
                    INTER_REGULAR = Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes()));
                }
            }

            try (var resource = Text.class.getResourceAsStream("/fonts/Inter-Bold.ttf")) {
                if (resource != null) {
                    INTER_BOLD = Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes()));
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load Inter font");
        }

        FONT_COLLECTION = new FontCollection();
        FONT_COLLECTION.setDefaultFontManager(INTER_FONT_MGR);
        FONT_COLLECTION.setTestFontManager(INTER_FONT_MGR);
        FONT_COLLECTION.setEnableFallback(false);
    }

    public static Paragraph basicPara(String text, int color, float size) {
        TextStyle style = new TextStyle();
        style.setColor(color);
        style.setFontSize(size);
        style.setFontFamily("Inter");

        ParagraphStyle paraStyle = new ParagraphStyle();
        paraStyle.setTextStyle(style);

        ParagraphBuilder builder = new ParagraphBuilder(paraStyle, FONT_COLLECTION);
        builder.pushStyle(style);
        builder.addText(text);
        builder.popStyle();

        return builder.build();
    }

    public static Node para(Paragraph para) {
        return para(constantSupplier(para));
    }

    public static Node para(Supplier<Paragraph> para) {
        return Node.builder()
                .layout(yoga -> {
                    Yoga.YGNodeStyleSetMaxWidthPercent(yoga, 100f);
                    Yoga.YGNodeSetMeasureFunc(yoga, (node, width, widthMode, height, heightMode, __result) -> {
                        var p = para.get();
                        p.layout(width);
                        Yoga.YGNodeStyleSetMinWidth(yoga, p.getMinIntrinsicWidth());
                        __result.height(p.getHeight());
                        __result.width(p.getMaxIntrinsicWidth());
                    });
                })
                .paint((canvas, yoga) -> {
                    para.get().paint(canvas, 0, 0);
                })
                .build();
    }

    public static Node line(Supplier<TextLine> line, Supplier<Integer> color) {
        return Node.builder()
                .layout(yoga -> {
                    Yoga.YGNodeStyleSetWidth(yoga, line.get().getWidth());
                    Yoga.YGNodeStyleSetHeight(yoga, line.get().getHeight());
                })
                .paint((canvas, yoga) -> {
                    try (var paint = new Paint()) {
                        paint.setColor(color.get());
                        canvas.drawTextLine(line.get(), 0, -line.get().getAscent(), paint);
                    }
                })
                .build();
    }

    public static TextLine basicTextLine(String string, float size) {
        Font font = new Font();
        font.setTypeface(Text.INTER_REGULAR);
        font.setSize(size);
        return TextLine.make(string, font);
    }
}
