package test;

import org.jsignal.prop.GeneratePropComponent;
import org.jsignal.prop.Prop;

import java.util.function.Supplier;

@GeneratePropComponent
public class TestComponent {
  @Prop
  Supplier<Integer> property;
}
