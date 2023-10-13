package com.github.wilgaboury.examples.todo;

import static com.github.wilgaboury.jsignal.ReactiveUtil.createProvider;

public class TodoList {
//    public static final Context<Supplier<Integer>> ItemIdxContext = ReactiveUtil.createContext(() -> -1);
//
//    public static Component create(Supplier<List<Item>> items) {
//        return Flex.builder().column().children((
//                ReactiveList.createMapped(items, TodoList::createItem)
//        ));
//    }
//
//    private static Component createItem(Item value, Supplier<Integer> idx) {
//        return createProvider(ItemIdxContext.provide(idx), () ->
//                Flex.builder().column().children(ReactiveList.of(Text.create(value.text())))
//        );
//    }
//
//    public record Item(Signal<Boolean> checked, Signal<String> text) {
//    }
}
