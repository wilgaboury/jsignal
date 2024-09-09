<img align="left" height="60px" hspace="10" src="resources/logo.svg"/>

# JSignal

**[Website](https://wilgaboury.github.io/jsignal) • [Javadocs](https://wilgaboury.github.io/jsignal/javadoc/index.html) • [Discord](https://discord.gg/YN7tek3CM2)**

A declarative GUI library for Java desktop applications that takes strong inspiration
from [SolidJS](https://www.solidjs.com/).

## Module Disambiguation

| Module         | Description                                                                                                                                                                          |
|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [rx](./rx)     | reactive primitives                                                                                                                                                                  |
| [ui](./ui)     | UI system built on JSignal, [Skia](https://skia.org/) ([Skija](https://github.com/HumbleUI/Skija/)), [JWM](https://github.com/HumbleUI/JWM), and [Yoga](https://www.yogalayout.dev/) |
| [std](./std)   | standard component library                                                                                                                                                           |
| [prop](./prop) | annotation processor for component properties                                                                                                                                        |

## Key Features

* Fine-grained reactivity: node tree creation, layout and painting is incremental and efficient
* Automatic dependency tracking: simply accessing state subscribes to it
* Hotswap: Code changes intelligently trigger parts of the component tree to rerender without stopping the application
* Skia graphics: powerful canvas API with support for software and hardware rendering
* Yoga layout: familiar, web-standard Flexbox layout

## Example

```java
public class Counter extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Counter");
      window.setContentSize(250, 250);
      new UiWindow(window, Counter::new);
    });
  }

  private final Signal<Integer> count = Signal.create(0);

  @Override
  public Element render() {
    return EzNode.builder()
      .layout(EzLayout.builder()
        .fill()
        .center()
        .column()
        .gap(10f)
        .build()
      )
      .children(
        Para.builder()
          .string(() -> "Count: " + count.get())
          .customize(style -> style.setTextStyle(text -> text
            .setFontSize(20f)
            .setColor(EzColors.BLUE_500)
          ))
          .build(),
        Button.builder()
          .color(EzColors.BLUE_300)
          .action(() -> count.accept(c -> c + 1))
          .children(() -> Para.fromString("Increment"))
          .build()
      )
      .build();
  }
}
```

![Counter Example Screencapture](resources/counter_border.gif)
