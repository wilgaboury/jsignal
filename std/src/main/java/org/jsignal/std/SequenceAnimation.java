package org.jsignal.std;

import java.util.List;

public class SequenceAnimation {
  private final List<IntervalAnimation> sequence;
  private int cur;

  public SequenceAnimation(List<IntervalAnimation> sequence) {
    this.sequence = sequence;
    this.cur = 0;
  }

  public SequenceAnimation(IntervalAnimation... sequence) {
    this(List.of(sequence));
  }

  public void start() {

  }

  public void stop() {

  }
}
