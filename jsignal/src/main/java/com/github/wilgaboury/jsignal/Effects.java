package com.github.wilgaboury.jsignal;

import com.github.wilgaboury.jsignal.interfaces.EffectLike;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

// TODO: delete class and simply move into Signal
public record Effects(Map<Integer, EffectRef> effects) implements Runnable {
    @Override
    public void run() {
        var batch = Batch.batch.get();
        batch.run(() -> {
            Iterator<EffectRef> itr = effects.values().iterator();
            while (itr.hasNext()) {
                EffectRef ref = itr.next();
                Optional<EffectLike> effect = ref.getEffect();

                if (effect.isEmpty() || effect.get().isDisposed())
                    itr.remove();
                else
                    batch.add(ref);
            }
        });
    }
}
