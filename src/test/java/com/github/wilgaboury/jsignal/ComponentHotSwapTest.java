package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigui.Node;
import com.github.wilgaboury.sigui.Sigui;
import org.junit.jupiter.api.Test;


public class ComponentHotSwapTest {
    @Test
    public static void main(String[] args) {
        Ref<Computed<Node>> computed = new Ref<>();
        Sigui.start(() -> {
            Component component = new MyComponent();
            computed.set(ReactiveUtil.createComputed(component));
        });
    }

    public static class MyComponent extends Component {
        @Override
        public Node get() {
            System.out.println("bruh9");
            return null;
        }
    }
}
