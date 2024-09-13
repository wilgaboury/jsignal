package org.jsignal.std;

public interface EasingFunction {
  float calculate(float x);

  default float apply(float x, float start, float end) {
    return start + calculate(x) * (end - start);
  }

  static float linear(float x) {
    return x;
  }

  static float easeInSine(float x) {
    return 1f - (float) Math.cos((x * Math.PI) / 2f);
  }

  static float easeOutBounce(float x) {
    float n1 = 7.5625f;
    float d1 = 2.75f;

    if (x < 1 / d1) {
      return n1 * x * x;
    } else if (x < 2 / d1) {
      return n1 * (x -= 1.5f / d1) * x + 0.75f;
    } else if (x < 2.5 / d1) {
      return n1 * (x -= 2.25f / d1) * x + 0.9375f;
    } else {
      return n1 * (x -= 2.625f / d1) * x + 0.984375f;
    }
  }
}
