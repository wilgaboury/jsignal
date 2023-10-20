package com.github.wilgaboury.sigui;

import com.github.wilgaboury.jsignal.Ref;
import com.github.wilgaboury.sigwig.EzColors;
import io.github.humbleui.skija.*;
import io.github.humbleui.skija.paragraph.*;
import org.lwjgl.util.yoga.Yoga;

import java.io.IOException;
import java.util.function.Supplier;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class Text {
    private static final Typeface typeface;
    private static final FontCollection fontCollection;
    static {
        try {
            typeface = Typeface.makeFromData(Data.makeFromBytes(
                    Text.class.getResourceAsStream("/fonts/Inter-Regular.ttf")
                            .readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TypefaceFontProvider provider = new TypefaceFontProvider();
        provider.registerTypeface(typeface);

        fontCollection = new FontCollection();
        fontCollection.setDefaultFontManager(provider);
        fontCollection.setTestFontManager(provider);
        fontCollection.setEnableFallback(false);
    }

    public static Node create(Supplier<String> textSignal) {
        Paragraph para = createPara(untrack(textSignal));
        var window = useContext(SiguiWindow.CONTEXT);

        Ref<Long> yogaRef = new Ref<>();

        createEffect(onDefer(textSignal, (text) -> {
            Yoga.YGNodeMarkDirty(yogaRef.get());
            window.requestLayout();
        }));

        return Node.builder()
                .setLayout(yoga -> {
                    yogaRef.set(yoga);
                    Yoga.YGNodeStyleSetMaxWidthPercent(yoga, 100f);
//                    Yoga.YGNodeStyleSetHeightPercent(yoga, 100f);
                    Yoga.YGNodeSetMeasureFunc(yoga, (node, width, widthMode, height, heightMode, __result) -> {
                        Paragraph p = para.layout(width);
                        __result.height(p.getHeight());
                        __result.width(p.getMaxIntrinsicWidth());
                    });
                })
                .setPaint((canvas, yoga) -> {
//                    try (var paint = new Paint()) {
//                        paint.setColor(EzColors.LIME_800);
//                        canvas.drawCircle(0, 0, 5, paint);
//                        TextLine line = TextLine.make("HELLO", new Font(typeface));
//                        canvas.drawTextLine(line, 0, 0, paint);
//                    }
                    para.paint(canvas, 0, 0);
                })
                .build();
    }


    private static Paragraph createPara(String text) {
        TextStyle style = defaultStyle();
        ParagraphStyle paraStyle = new ParagraphStyle();
        paraStyle.setTextStyle(style);
        ParagraphBuilder builder = new ParagraphBuilder(paraStyle, fontCollection);
        builder.pushStyle(style);
        builder.addText(text);
        builder.popStyle();
        return builder.build();
    }

    private static TextStyle defaultStyle() {
        TextStyle style = new TextStyle();
        style.setColor(EzColors.BLACK);
        style.setFontSize(20f);
        style.setFontFamily("Inter");
//        style.setTypeface(typeface);
//        Paint paint = new Paint();
//        paint.setColor(EzColors.CYAN_600);
//        style.setForeground(paint);
        return style;
    }
}
