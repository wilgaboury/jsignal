package com.github.wilgaboury.jsignal;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class Effects implements Runnable {
    private final Map<Integer, EffectRef> effects;

    public Effects(Map<Integer, EffectRef> effects) {
        this.effects = effects;
    }

    public void add(Effect effect, Executor executor) {
        effects.putIfAbsent(effect.getId(), new EffectRef(effect, executor));
    }

    @Override
    public void run() {
        var env = ReactiveEnv.getInstance().get();
        if (env.isInBatch())
            forEach(env::addBatchedListener);
        else
            forEach(EffectRef::run);
    }

    private void forEach(Consumer<EffectRef> listenerConsumer) {
        Iterator<EffectRef> itr = effects.values().iterator();
        while (itr.hasNext()) {
            EffectRef ref = itr.next();
            Optional<Effect> effect = ref.getEffect();

            if (effect.isEmpty() || effect.get().isDisposed())
                itr.remove();
            else
                listenerConsumer.accept(ref);
        }
    }
}
