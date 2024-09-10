package org.jsignal.std;

import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.Typeface;
import io.github.humbleui.skija.paragraph.FontCollection;
import io.github.humbleui.skija.paragraph.Paragraph;
import io.github.humbleui.skija.paragraph.ParagraphBuilder;
import io.github.humbleui.skija.paragraph.TypefaceFontProvider;
import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Context;
import org.jsignal.rx.Effect;
import org.jsignal.rx.Provider;
import org.jsignal.std.ez.EzNode;
import org.jsignal.ui.Element;
import org.jsignal.ui.UiThread;
import org.jsignal.ui.UiWindow;
import org.jsignal.ui.layout.LayoutConfig;
import org.jsignal.ui.paint.SurfacePaintCacheStrategy;
import org.jsignal.ui.paint.UpgradingPaintCacheStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.createMemo;

@GeneratePropComponent
public non-sealed class Para extends ParaPropComponent {
  private static final FontCollection defaultCollection = new FontCollection();

  public static Context<FontCollection> fontsContext = Context.create(defaultCollection);

  public static final TypefaceFontProvider fontManager = new TypefaceFontProvider();

  static {
    defaultCollection.setDefaultFontManager(io.github.humbleui.skija.FontMgr.getDefault());
    defaultCollection.setDynamicFontManager(fontManager);
  }

  public static void loadFontFromResource(String location) throws IOException {
    try (InputStream resource = Para.class.getResourceAsStream(location)) {
      if (resource != null) {
        fontManager.registerTypeface(Typeface.makeFromData(Data.makeFromBytes(resource.readAllBytes())));
      }
    }
  }

  @Prop(oneofKey = "content")
  Supplier<String> string;
  @Prop(oneofKey = "content")
  Supplier<Paragraph> para;
  @Prop
  Supplier<ParaStyle> style;
  @Prop
  Function<ParaStyle.Builder, ParaStyle.Builder> customize;
  @Prop
  Supplier<Boolean> line = Constant.of(false);

  public Para() {}

  public static Para from(Paragraph paragraph) {
    return Para.builder().para(paragraph).build();
  }

  public static Para from(Supplier<Paragraph> paragraph) {
    return Para.builder().para(paragraph).build();
  }

  public static Para fromString(String string) {
    return Para.builder().string(string).build();
  }

  public static Para fromString(Supplier<String> string) {
    return Para.builder().string(string).build();
  }

  @Override
  protected void onBuild() {
    if (para == null) {
      if (style == null) {
        if (customize == null) {
          style = ParaStyle.context.use();
        } else {
          style = createMemo(() -> customize.apply(ParaStyle.context.use().get().toBuilder()).build());
        }
      }
      para = createMemo(() -> {
        var result = new ParagraphBuilder(style.get().toSkia(), fontsContext.use());
        result.addText(string.get());
        return result.build();
      });
    }
  }

  @Override
  public Element render() {
    var provider = Provider.get();
    return EzNode.builder()
      .ref(meta -> meta.setPaintCacheStrategy(
        new UpgradingPaintCacheStrategy(SurfacePaintCacheStrategy::new)))
      .id("para")
      .layout(config -> {
        UiThread.queueMicrotask(() -> {
          provider.provide(() -> {
            Effect.create(() -> {
              // track state
              para.get();
              line.get();

              config.markDirty();
              UiWindow.context.use().requestLayout();
            });
          });
        });
        config.setMeasure((width, widthMode, height, heightMode) -> {
          var p = para.get();
          p.layout(width);
          float intrinsicWidth = Math.round(p.getMaxIntrinsicWidth() + 0.5f);
          return new LayoutConfig.Size(
            line.get() ? intrinsicWidth : Math.min(width, intrinsicWidth),
            p.getHeight()
          );
        });
      })
      .paint((canvas, layout) -> {
        var p = para.get();
        // Extra layout call prevents wierd cutoff errors.
        // I know you, at some point you will see this and think, "this is a repeat call to
        // layout, let's just delete this line to make the code simpler and faster".
        // DON'T DELETE IT!
        p.layout(layout.getWidth());
        p.paint(canvas, 0f, 0f);
      })
      .build();
  }

  public Paragraph getParagraph() {
    return para.get();
  }
}
