package com.github.wilgaboury.jsignal.examples.todo;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.ListUtil;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.interfaces.Signal;
import com.github.wilgaboury.jsignal.sigui.Column;
import com.github.wilgaboury.jsignal.sigui.Node;
import com.github.wilgaboury.jsignal.sigui.Row;
import com.github.wilgaboury.jsignal.sigui.Text;

import java.util.List;
import java.util.function.Supplier;

public class TodoList {
    public static final Context<Supplier<Integer>> ItemIdxContext = ReactiveUtil.createContext(() -> -1);
    public static Node create(Supplier<List<Item>> items) {

        return Column.create((ListUtil.map(items, (value, idx) ->
                ReactiveUtil.createProvider(ItemIdxContext.provide(idx), () ->
                        Row.create(ListUtil.fixed(Text.create(value.getText())))))));
    }

    public static class Item {
        private final Signal<Boolean> checked;
        private final Signal<String> text;

        public Item(Signal<Boolean> checked, Signal<String> text) {
            this.checked = checked;
            this.text = text;
        }

        public Signal<Boolean> getChecked() {
            return checked;
        }

        public Signal<String> getText() {
            return text;
        }
    }
}
