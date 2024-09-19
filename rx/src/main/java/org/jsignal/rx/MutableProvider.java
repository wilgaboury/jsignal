package org.jsignal.rx;

import java.util.Arrays;
import java.util.HashMap;

public class MutableProvider {
  private final HashMap<Context<?>, Object> contexts;

  public MutableProvider() {
    this.contexts = new HashMap<>();
  }

  public MutableProvider(MutableProvider that) {
    this.contexts = new HashMap<>(that.contexts);
  }

  public void add(Iterable<Provider.Entry> entries) {
    for (var entry : entries) {
      contexts.put(entry.getContext(), entry.getValue());
    }
  }

  public void add(Provider.Entry... entries) {
    add(Arrays.asList(entries));
  }

  @SuppressWarnings("unchecked")
  public <T> T use(Context<T> context) {
    var obj = contexts.get(context);
    if (obj != null) {
      return (T) obj;
    } else {
      return context.getDefaultValue();
    }
  }
}
