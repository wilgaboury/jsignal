package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

import static com.github.wilgaboury.jsignal.ReactiveUtil.*;

public class Effects implements Runnable {
    private final Map<Integer, EffectRef> effects;

    public Effects(Map<Integer, EffectRef> effects) {
        this.effects = effects;
    }

    public void add(EffectLike effect, Executor executor) {
        effects.putIfAbsent(effect.getId(), new EffectRef(effect, executor));
    }

    @Override
    public void run() {
        var batch = BATCH.get();
        batch.run(() -> {
            Iterator<EffectRef> itr = effects.values().iterator();
            while (itr.hasNext()) {
                EffectRef ref = itr.next();
                Optional<EffectLike> effect = ref.getEffect();

                if (effect.isEmpty() || effect.get().isDisposed())
                    itr.remove();
                else
                    batch.add(ref.getId(), ref);
            }
        });
    }
}
