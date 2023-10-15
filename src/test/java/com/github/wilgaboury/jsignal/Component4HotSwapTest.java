package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.experimental.Component4;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.Sigui;
import org.junit.jupiter.api.Test;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class Component4HotSwapTest {
    @Test
    public static void main(String[] args) {
        Ref<Computed<Node>> computed = new Ref<>();
        Sigui.start(() -> {
            Component4 component = new MyComponent();
            computed.set(createComputed(component));
        });
    }

    public static class MyComponent extends Component4 {
        @Override
        public Node get() {
            System.out.println("bruh6");
            return null;
        }
    }
}
