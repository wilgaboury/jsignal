package com.github.wilgaboury.examples.todo;

import com.github.wilgaboury.jsignal.Context;
import com.github.wilgaboury.jsignal.ReactiveList;
import com.github.wilgaboury.jsignal.ReactiveUtil;
import com.github.wilgaboury.jsignal.interfaces.Signal;
import com.github.wilgaboury.sigui.Component;
import com.github.wilgaboury.sigwig.Column;
import com.github.wilgaboury.sigwig.Text;

import java.util.List;
import java.util.function.Supplier;

public class TodoList {
    public static final Context<Supplier<Integer>> ItemIdxContext = ReactiveUtil.createContext(() -> -1);

    public static Component create(Supplier<List<Item>> items) {
        return Column.create((ReactiveList.createMapped(items, (value, idx) ->
                createProvider(ItemIdxContext.provide(idx), () ->
                        Row.create(ReactiveList.of(Text.create(value.getText())))
                )
        )));
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
