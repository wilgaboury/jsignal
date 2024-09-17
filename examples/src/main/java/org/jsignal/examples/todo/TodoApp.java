package org.jsignal.examples.todo;

import org.jsignal.rx.Effect;
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

import java.util.ArrayList;
import java.util.List;

import static org.jsignal.rx.RxUtil.batch;
import static org.jsignal.rx.RxUtil.createMapped;
import static org.jsignal.ui.Nodes.compose;
import static org.jsignal.ui.layout.Insets.insets;

public class TodoApp extends Component {
  public static void main(String[] args) {
    UiThread.start(() -> {
      var window = UiUtil.createWindow();
      window.setTitle("Todo Application");
      window.setContentSize(500, 500);
      ParaStyle.context.customize(sb -> sb.textStyleBuilder(tsb -> tsb
        .color(EzColors.BLACK)
      ))
        .provide(() -> new UiWindow(window, TodoApp::new));
    });
  }

  private final Signal<String> todo = Signal.create("");
  private final Signal<List<String>> todos = Signal.create(new ArrayList<>(List.of(
    "ONE",
    "TWO",
    "THREE"
  )));

  @Override
  protected Element render() {
    var test = createMapped(todos, (value, idx) -> "TEST");
    Effect.create(() -> System.out.println(test.get().size()));

    return Scroll.builder()
      .children(
        Node.builder()
          .layoutBuilder(lb -> lb
            .column()
            .center()
            .gap(16f)
          )
          .children(compose(
            Para.builder()
              .string("Todo List")
              .styleBuilder(sb -> sb.textStyleBuilder(tsb -> tsb
                .fontSize(32f)
              ))
              .build(),
            Node.builder()
              .layoutBuilder(lb -> lb
                .row()
                .wrap()
                .gap(16f)
              )
              .children(compose(
                TextInput.builder()
                  .content(todo)
                  .onInput(todo)
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
              .build(),
            Nodes.forEach(todos, (content, idx) ->
              Node.builder()
                .paint(CardPainter.builder()
                  .radius(16f)
                  .build()
                )
                .layoutBuilder(lb -> lb
                  .padding(insets(16f).pixels())
                )
                .children(
                  Para.builder()
                    .string(content)
                    .styleBuilder(sb -> sb.textStyleBuilder(tsb -> tsb
                      .fontSize(16f)
                    ))
                    .build()
                )
                .build()
            )
          ))
          .build()
      )
      .build();
  }
}
