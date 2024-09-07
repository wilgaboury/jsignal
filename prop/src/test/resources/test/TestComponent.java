package test;

import org.jsignal.prop.GeneratePropBuilder;
import org.jsignal.prop.Prop;

import java.util.function.Supplier;

@GeneratePropBuilder
public class TestComponent {
  @Prop
  Supplier<Integer> property;
}
