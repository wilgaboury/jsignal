package com.github.wilgaboury.jsignal;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class Listeners {
    private final Map<Integer, WeakReference<EffectHandle>> listeners;

    public Listeners(Map<Integer, WeakReference<EffectHandle>> listeners) {
        this.listeners = listeners;
    }

    public void addListener(EffectHandle listener) {
        if (listener != null)
            listeners.putIfAbsent(listener.getId(), new WeakReference<>(listener));
    }

    public void notifyListeners() {
        var env = ReactiveEnv.getInstance().get();
        if (env.isInBatch())
            forEachListener(env::addBatchedListener);
        else
            forEachListener(EffectHandle::run);
    }

    private void forEachListener(Consumer<EffectHandle> listenerConsumer) {
        Iterator<WeakReference<EffectHandle>> itr = listeners.values().iterator();
        while (itr.hasNext()) {
            EffectHandle listener = itr.next().get();

            if (listener == null || listener.isDisposed())
                itr.remove();
            else
                listenerConsumer.accept(listener);
        }
    }
}
