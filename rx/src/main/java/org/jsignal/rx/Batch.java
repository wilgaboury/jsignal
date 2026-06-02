package org.jsignal.rx;

public interface Batch {
  void add(EffectRef ref);
  void commit();
}
