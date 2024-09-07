package test;

public abstract class TestComponentComponent extends org.jsignal.ui.Component {
  public static class Builder {
    private final TestComponent component;

    public Builder() {
      this.component = new TestComponent();
    }

    public Builder property(java.util.function.Supplier<Integer> property) {
      this.component.property = org.jsignal.rx.RxUtil.createMemo(property);
      return this;
    }

    public Builder property(Integer property) {
      this.component.property = org.jsignal.rx.Constant.of(property);
      return this;
    }

    public TestComponent build() {
      return component;
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
