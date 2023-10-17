package com.github.wilgaboury.sigui;

import java.util.function.Supplier;

public abstract class Component {
    public abstract Nodes render();

//    public static <N extends Nodes> Component<N> from(N nodes) {
//        return new Constant<>(nodes);
//    }

//    private static class Constant<N extends Nodes> extends Component<N> {
//        private final N node;
//
//        public Constant(N node) {
//            this.node = node;
//        }
//
//        @Override
//        public N get() {
//            return node;
//        }
//    }
}
