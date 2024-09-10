package test;

import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;
import org.jsignal.ui.Element;
import org.jsignal.ui.Nodes;

import java.util.function.Supplier;

@GeneratePropComponent
public final class TestComponent extends TestComponentPropComponent {
  @Prop
  Supplier<Integer> property;

  @Override
  protected Element render() {
    return Nodes.empty();
  }
}
