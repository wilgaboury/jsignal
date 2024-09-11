package org.jsignal.std;

public interface InterpolationFunction {
  float interpolate(float frac, float start, float end);

  static float lerp(float frac, float start, float end) {
    return start + frac * (end - start);
  }
}
