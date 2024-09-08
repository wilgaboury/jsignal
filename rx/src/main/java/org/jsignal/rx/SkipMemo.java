package org.jsignal.rx;

/**
 * marker interface for skipping memoization
 */
public interface SkipMemo {
  static boolean shouldSkip(Object object) {
    return object instanceof SkipMemo;
  }
}
