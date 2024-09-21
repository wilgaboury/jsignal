package org.jsignal.std;

import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.Typeface;
import io.github.humbleui.skija.paragraph.FontCollection;
import io.github.humbleui.skija.paragraph.Paragraph;
import io.github.humbleui.skija.paragraph.ParagraphBuilder;
import io.github.humbleui.skija.paragraph.TypefaceFontProvider;
import org.jsignal.prop.BuildProps;
import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.rx.Context;
import org.jsignal.rx.Effect;
import org.jsignal.rx.Ref;
import org.jsignal.rx.Signal;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.layout.LayoutConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jsignal.rx.RxUtil.*;

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

  @BuildProps
  public static class Transitive {
    @Prop(oneofKey = "content")
    Supplier<String> string;

    @Prop
    Function<ParaStyle.Builder, ParaStyle.Builder> styleBuilder;
  }

  @Prop(oneofKey = "content")
  Supplier<Paragraph> para;
  @Prop
  Supplier<ParaStyle> style = ParaStyle.context.use();

  private final Signal<Float> height = Signal.empty();
  private final Ref<Node> ref = new Ref<>();

  public Para() {}

  public Node getRef() {
    return ref.get();
  }

  @Override
  protected void onBuild(Transitive transitive) {
    if (transitive.styleBuilder != null) {
      this.style = createMemo(() -> transitive.styleBuilder.apply(style.get().toBuilder()).build());
    }

    if (para == null) {
      para = createMemo(() -> {
        var result = new ParagraphBuilder(style.get().toSkia(), fontsContext.use());
        result.addText(transitive.string.get());
        return result.build();
      });
    }
  }

  @Override
  public Element render() {
    onResolve(() -> Effect.create(onDefer(para, () -> ref.get().getLayoutConfig().markDirty())));
    return Node.builder()
      .ref(ref)
      .layout(config ->
        config.setMeasure((width, widthMode, height, heightMode) -> {
          var layoutWidth = widthMode == LayoutConfig.MeasureMode.UNDEFINED ? Float.POSITIVE_INFINITY : width;
          var layoutHeight = heightMode == LayoutConfig.MeasureMode.UNDEFINED ? Float.POSITIVE_INFINITY : height;
          var p = ignore(para);
          p.layout(layoutWidth);
          return new LayoutConfig.Size(
            Math.min(p.getMaxIntrinsicWidth(), layoutWidth),
            Math.min(p.getHeight(), layoutHeight)
          );
        })
      )
      .paint((canvas, layout) -> {
        var p = para.get();
        p.layout(layout.getWidth());
        p.paint(canvas, 0f, 0f);
      })
      .build();
  }

  public Paragraph getParagraph() {
    return para.get();
  }

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
}
