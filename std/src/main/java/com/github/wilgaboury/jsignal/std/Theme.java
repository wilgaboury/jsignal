package com.github.wilgaboury.jsignal.std;

import com.github.wilgaboury.jsignal.rx.Context;
import com.github.wilgaboury.jsignal.rx.Signal;
import com.github.wilgaboury.jsignal.std.ez.EzColors;

public class Theme {
  public static final Context<Signal<Theme>> context = Context.create(Signal.create(Theme.builder().build()));

  private final int light;
  private final int dark;
  private final int primary;
  private final int secondary;
  private final int info;
  private final int success;
  private final int warning;
  private final int error;

  public Theme(Builder builder) {
    this.light = builder.light;
    this.dark = builder.dark;
    this.primary = builder.primary;
    this.secondary = builder.secondary;
    this.info = builder.info;
    this.success = builder.success;
    this.warning = builder.warning;
    this.error = builder.error;
  }

  public int getLight() {
    return light;
  }

  public int getDark() {
    return dark;
  }

  public int getPrimary() {
    return primary;
  }

  public int getSecondary() {
    return secondary;
  }

  public int getInfo() {
    return info;
  }

  public int getSuccess() {
    return success;
  }

  public int getWarning() {
    return warning;
  }

  public int getError() {
    return error;
  }

  public Builder toBuilder() {
    return builder()
      .setLight(light)
      .setDark(dark)
      .setPrimary(primary)
      .setSecondary(secondary)
      .setInfo(info)
      .setSuccess(success)
      .setWarning(warning)
      .setError(error);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private int light = EzColors.NEUTRAL_200;
    private int dark = EzColors.NEUTRAL_700;
    private int primary = EzColors.INDIGO_600;
    private int secondary = EzColors.PINK_500;
    private int info = EzColors.SKY_400;
    private int success = EzColors.EMERALD_400;
    private int warning = EzColors.AMBER_400;
    private int error = EzColors.RED_400;

    public Builder setLight(int light) {
      this.light = light;
      return this;
    }

    public Builder setDark(int dark) {
      this.dark = dark;
      return this;
    }

    public Builder setPrimary(int primary) {
      this.primary = primary;
      return this;
    }

    public Builder setSecondary(int secondary) {
      this.secondary = secondary;
      return this;
    }

    public Builder setInfo(int info) {
      this.info = info;
      return this;
    }

    public Builder setSuccess(int success) {
      this.success = success;
      return this;
    }

    public Builder setWarning(int warning) {
      this.warning = warning;
      return this;
    }

    public Builder setError(int error) {
      this.error = error;
      return this;
    }

    public Theme build() {
      return new Theme(this);
    }
  }
}