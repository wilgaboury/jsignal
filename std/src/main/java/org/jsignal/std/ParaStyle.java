package org.jsignal.std;

import io.github.humbleui.skija.paragraph.*;
import jakarta.annotation.Nullable;
import org.jsignal.prop.GeneratePropHelper;
import org.jsignal.prop.Prop;
import org.jsignal.rx.ComputedContext;
import org.jsignal.rx.Constant;
import org.jsignal.rx.Provider;

import java.util.function.Function;

@GeneratePropHelper
public final class ParaStyle extends ParaStylePropHelper {
  public static class Context extends ComputedContext<ParaStyle> {
    public Context() {
      super(Constant.of(builder().build()));
    }

    public Provider.Entry customize(Function<ParaStyle.Builder, ParaStyle.Builder> customize) {
      return withComputed(s -> customize.apply(s.toBuilder()).build());
    }
  }

  public static final Context context = new Context();

  @Prop
  ParaTextStyle textStyle = ParaTextStyle.builder().build();

  @Prop
  Function<ParaTextStyle.Builder, ParaTextStyle.Builder> customizeTextStyle;

  @Prop
  @Nullable
  Alignment alignment;

  @Prop
  @Nullable
  Float height;

  @Prop
  @Nullable
  Direction direction;

  @Prop
  @Nullable
  StrutStyle strutStyle;

  @Prop
  @Nullable
  String ellipsis;

  @Prop
  @Nullable
  HeightMode heightMode;

  @Prop
  @Nullable
  Long maxLinesCount;

  @Override
  public void onBuild() {
    if (customizeTextStyle != null) {
      textStyle = customizeTextStyle.apply(textStyle.toBuilder()).build();
    }
  }

  public ParagraphStyle toSkia() {
    var para = new ParagraphStyle();
    para.setTextStyle(textStyle.toSkia());
    if (alignment != null) para.setAlignment(alignment);
    if (height != null) para.setHeight(height);
    if (direction != null) para.setDirection(direction);
    if (strutStyle != null) para.setStrutStyle(strutStyle);
    if (ellipsis != null) para.setEllipsis(ellipsis);
    if (heightMode != null) para.setHeightMode(heightMode);
    if (maxLinesCount != null) para.setMaxLinesCount(maxLinesCount);
    return para;
  }
}
