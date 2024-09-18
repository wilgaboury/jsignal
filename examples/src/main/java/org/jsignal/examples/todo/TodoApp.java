package org.jsignal.examples.todo;

import org.jsignal.rx.Signal;
import org.jsignal.std.Button;
import org.jsignal.std.Para;
import org.jsignal.std.ParaStyle;
import org.jsignal.std.Scroll;
import org.jsignal.std.TextInput;
import org.jsignal.std.ez.EzColors;
import org.jsignal.ui.Component;
import org.jsignal.ui.Element;
import org.jsignal.ui.Node;
import org.jsignal.ui.Nodes;
import org.jsignal.ui.UiThread;
import org.jsignal.ui.UiUtil;
import org.jsignal.ui.UiWindow;
import org.jsignal.ui.layout.CompositeLayouter;
import org.jsignal.ui.layout.LayoutConfig;

import java.util.ArrayList;
import java.util.List;

import static org.jsignal.rx.RxUtil.batch;
import static org.jsignal.ui.Nodes.compose;
import static org.jsignal.ui.layout.Insets.insets;
import static org.jsignal.ui.layout.LayoutValue.percent;
import static org.jsignal.ui.layout.LayoutValue.pixel;

public class TodoApp extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> UiUtil.conditionallyProvideHotswapInstrumentation(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Todo Application");
      window.setContentSize(500, 500);
      ParaStyle.context.customize(sb -> sb.textStyleBuilder(tsb -> tsb
        .color(EzColors.BLACK)
      ))
        .provide(() -> new UiWindow(window, TodoApp::new));
    }));
  }

  private final Signal<String> todo = Signal.create("");
  private final Signal<List<String>> todos = Signal.create(new ArrayList<>(List.of(
//    "ONE",
//    "TWO",
//    "THREE",
//    "1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234 1234"
  )));

  @Override
  protected Element render() {
    return Scroll.builder()
      .outerLayout(CompositeLayouter.builder()
        .fill()
        .overflow()
        .build()
      )
      .innerLayout(CompositeLayouter.builder()
        .width(percent(100f))
        .maxWidth(percent(100f))
        .row()
        .alignItems(LayoutConfig.Align.START)
        .justify(LayoutConfig.JustifyContent.CENTER)
        .build()
      )
      .children(
        Node.builder()
          .layoutBuilder(lb -> lb
            .grow(1f)
            .padding(insets(16f).pixels())
            .column()
            .center()
            .gap(16f)
            .justify(LayoutConfig.JustifyContent.CENTER)
            .alignItems(LayoutConfig.Align.STRETCH)
            .maxWidth(pixel(1024f))
          )
          .children(compose(
            Node.builder()
              .layoutBuilder(lb -> lb
                .column()
                .gap(16f)
                .alignItems(LayoutConfig.Align.CENTER)
              )
              .children(compose(
//                Para.builder()
//                  .string("Todo List")
//                  .styleBuilder(sb -> sb.textStyleBuilder(tsb -> tsb
//                    .fontSize(32f)
//                  ))
//                  .build(),
                Node.builder()
                  .layoutBuilder(lb -> lb
                    .row()
                    .alignItems(LayoutConfig.Align.CENTER)
                    .wrap()
                    .gap(16f)
                  )
                  .children(compose(
                    TextInput.builder()
                      .content(todo)
                      .onInput(todo)
                      .children(para -> Node.builder()
                        .layoutBuilder(lb -> lb.minWidth(pixel(150f)))
                        .children(para)
                        .build()
                      )
                      .build(),
                    Button.builder()
                      .color(EzColors.BLUE_500)
                      .action(() -> batch(() -> {
                        todos.mutate(list -> {
                          list.add(todo.get());
                        });
                        todo.accept("");
                      }))
                      .children(() -> Para.fromString("Add"))
                      .build()
                  ))
                  .build()
              ))
              .build(),
            Nodes.forEach(todos, (content, idx) ->
              Node.builder()
                .paint(CardPainter.builder()
                  .radius(16f)
                  .build()
                )
                .layoutBuilder(lb -> lb
                  .maxWidth(percent(100f))
                  .padding(insets(16f).pixels())
                  .row()
                  .gap(16f)
                  .justify(LayoutConfig.JustifyContent.BETWEEN)
                  .alignItems(LayoutConfig.Align.CENTER)
                )
                .children(compose(
                  Node.builder()
                    .layoutBuilder(lb -> lb.shrink(1f))
                    .children(
                      Para.builder()
                        .string(content)
                        .styleBuilder(sb -> sb.textStyleBuilder(tsb -> tsb
                          .fontSize(16f)
                        ))
                        .build()
                    )
                    .build(),
                  Button.builder()
                    .color(EzColors.RED_600)
                    .action(() -> todos.mutate(list -> { list.remove((int)idx.get()); }))
                    .size(Button.Size.SM)
                    .children(() -> Para.fromString("Remove"))
                    .build()
                ))
                .build()
            )
          ))
          .build()
      )
      .build();
  }
}
