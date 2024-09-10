package org.jsignal.std;

import io.github.humbleui.skija.FontStyle;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Typeface;
import io.github.humbleui.skija.paragraph.BaselineMode;
import io.github.humbleui.skija.paragraph.DecorationStyle;
import io.github.humbleui.skija.paragraph.TextStyle;
import jakarta.annotation.Nullable;
import org.jsignal.prop.GeneratePropHelper;
import org.jsignal.prop.Prop;

import java.util.Collection;
import java.util.Collections;

@GeneratePropHelper
public final class ParaTextStyle extends ParaTextStylePropHelper {
  @Prop
  Collection<String> fontFamilies = Collections.emptyList();
  @Prop
  boolean placeholder = false;
  @Prop
  @Nullable
  FontStyle fontStyle;
  @Prop
  @Nullable
  Integer color;
  @Prop
  @Nullable
  Float fontSize;
  @Prop
  @Nullable
  Float fontHeight;
  @Prop
  @Nullable
  Paint foreground;
  @Prop
  @Nullable
  Paint background;
  @Prop
  @Nullable
  BaselineMode baselineMode;
  @Prop
  @Nullable
  DecorationStyle decorationStyle;
  @Prop
  @Nullable
  Float letterSpacing;
  @Prop
  @Nullable
  String locale;
  @Prop
  @Nullable
  Typeface typeface;
  @Prop
  @Nullable
  Float wordSpacing;

  public TextStyle toSkia() {
    var text = new TextStyle();
    text.setFontFamilies(fontFamilies.toArray(new String[0]));
    if (placeholder) text.setPlaceholder();
    if (fontStyle != null) text.setFontStyle(fontStyle);
    if (color != null) text.setColor(color);
    if (fontSize != null) text.setFontSize(fontSize);
    if (fontHeight != null) text.setHeight(fontHeight);
    if (foreground != null) text.setForeground(foreground);
    if (background != null) text.setBackground(background);
    if (baselineMode != null) text.setBaselineMode(baselineMode);
    if (decorationStyle != null) text.setDecorationStyle(decorationStyle);
    if (letterSpacing != null) text.setLetterSpacing(letterSpacing);
    if (locale != null) text.setLocale(locale);
    if (typeface != null) text.setTypeface(typeface);
    if (wordSpacing != null) text.setWordSpacing(wordSpacing);
    return text;
  }
}
