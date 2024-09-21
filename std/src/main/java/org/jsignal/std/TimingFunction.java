package org.jsignal.std;

/**
 * Functions copied from <a href="https://easings.net/" >https://easings.net/</a>
 */
public interface TimingFunction {
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

  static float easeOutSine(float x) {
    return (float) Math.sin((x * Math.PI) / 2);
  }

  static float easeInOutSine(float x) {
    return (float) -(Math.cos(Math.PI * x) - 1) / 2f;
  }

  static float easeInQuad(float x) {
    return x * x;
  }

  static float easeOutQuad(float x) {
    return 1f - (1f - x) * (1f - x);
  }

  static float easeInOutQuad(float x) {
    return x < 0.5 ? 2 * x * x : 1 - (float) Math.pow(-2 * x + 2, 2) / 2f;
  }

  static float easeInCubic(float x) {
    return x * x * x;
  }

  static float easeOutCubic(float x) {
    return 1f - (float) Math.pow(1 - x, 3);
  }

  static float easeInOutCubic(float x) {
    return x < 0.5f ? 4f * x * x * x : 1f - (float) Math.pow(-2 * x + 2, 3) / 2f;
  }

  static float easeInQuart(float x) {
    return x * x * x * x;
  }

  static float easeOutQuart(float x) {
    return 1f - (float) Math.pow(1 - x, 4);
  }

  static float easeInOutQuart(float x) {
    return x < 0.5f ? 8f * x * x * x * x : 1f - (float) Math.pow(-2 * x + 2, 4) / 2f;
  }

  static float easeInQuint(float x) {
    return x * x * x * x * x;
  }

  static float easeOutQuint(float x) {
    return 1f - (float) Math.pow(1 - x, 5);
  }

  static float easeInOutQuint(float x) {
    return x < 0.5f ? 16f * x * x * x * x * x : 1f - (float) Math.pow(-2 * x + 2, 5) / 2f;
  }

  static float easeInExpo(float x) {
    return x == 0f ? 0f : (float) Math.pow(2, 10 * x - 10);
  }

  static float easeOutExpo(float x) {
    return x == 1f ? 1f : 1f - (float) Math.pow(2, -10 * x);
  }

  static float easeInOutExpo(float x) {
    if (x == 0f) {
      return 0f;
    } else if (x == 1f) {
      return 1f;
    } else if (x < 0.5f) {
      return (float) Math.pow(2, 20 * x - 10) / 2f;
    } else {
      return (2f - (float) Math.pow(2, -20 * x + 10)) / 2f;
    }
  }

  static float easeInCirc(float x) {
    return 1f - (float) Math.sqrt(1 - Math.pow(x, 2));
  }

  static float easeOutCirc(float x) {
    return (float) Math.sqrt(1f - (float) Math.pow(x - 1, 2));
  }

  static float easeInOutCirc(float x) {
    return x < 0.5f
      ? (1f - (float) Math.sqrt(1f - (float) Math.pow(2 * x, 2))) / 2f
      : ((float) Math.sqrt(1f - (float) Math.pow(-2 * x + 2, 2)) + 1f) / 2f;
  }

  static float easeInBack(float x) {
    float c1 = 1.70158f;
    float c3 = c1 + 1;
    return c3 * x * x * x - c1 * x * x;
  }

  static float easeOutBack(float x) {
    float c1 = 1.70158f;
    float c3 = c1 + 1f;
    return 1f + c3 * (float) Math.pow(x - 1, 3) + c1 * (float) Math.pow(x - 1, 2);
  }

  static float easeInOutBack(float x) {
    float c1 = 1.70158f;
    float c2 = c1 * 1.525f;

    return x < 0.5f
      ? ((float) Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2f
      : ((float) Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2f;
  }

  static float easeInElastic(float x) {
    float c4 = (2 * (float) Math.PI) / 3;
    if (x == 0f) {
      return 0f;
    } else if (x == 1f) {
      return 1f;
    } else {
      return -(float) Math.pow(2, 10 * x - 10) * (float) Math.sin((x * 10 - 10.75) * c4);
    }
  }

  static float easeOutElastic(float x) {
    float c4 = (2 * (float) Math.PI) / 3;
    if (x == 0f) {
      return 0f;
    } else if (x == 1f) {
      return 1f;
    } else {
      return (float) Math.pow(2, -10 * x) * (float) Math.sin((x * 10 - 0.75) * c4) + 1;
    }
  }

  static float easeInOutElastic(float x) {
    float c5 = (2 * (float) Math.PI) / 4.5f;
    if (x == 0f) {
      return 0f;
    } else if (x == 1f) {
      return 1f;
    } else if (x < 0.5f) {
      return -((float) Math.pow(2, 20 * x - 10) * (float) Math.sin((20 * x - 11.125) * c5)) / 2;
    } else {
      return ((float) Math.pow(2, -20 * x + 10) * (float) Math.sin((20 * x - 11.125) * c5)) / 2 + 1;
    }
  }

  static float easeInBounce(float x) {
    return 1 - easeOutBounce(1 - x);
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

  static float easeInOutBounce(float x) {
    return x < 0.5
      ? (1 - easeOutBounce(1 - 2 * x)) / 2
      : (1 + easeOutBounce(2 * x - 1)) / 2;
  }
}
