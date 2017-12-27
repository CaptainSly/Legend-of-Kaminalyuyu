package com.lok.game.ability;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ReflectionPool;

public class AbilityPool {
    private final ObjectMap<Class<? extends Ability>, ReflectionPool<? extends Ability>> pools;

    public AbilityPool() {
	this.pools = new ObjectMap<Class<? extends Ability>, ReflectionPool<? extends Ability>>();
    }

    public <T extends Ability> T obtain(Class<T> type) {
	ReflectionPool<? extends Ability> pool = pools.get(type);

	if (pool == null) {
	    pool = new ReflectionPool<T>(type);
	    pools.put(type, pool);
	}

	return type.cast(pool.obtain());
    }

    public <T extends Ability> void free(T ability) {
	if (ability == null) {
	    throw new IllegalArgumentException("Ability cannot be null.");
	}

	@SuppressWarnings("unchecked")
	final ReflectionPool<T> pool = (ReflectionPool<T>) pools.get(ability.getClass());

	if (pool == null) {
	    return;
	}

	pool.free(ability);
    }

    public void freeAll(Array<? extends Ability> abilities) {
	if (abilities == null)
	    throw new IllegalArgumentException("Abilities cannot be null.");

	for (int i = 0, n = abilities.size; i < n; i++) {
	    final Ability ability = abilities.get(i);
	    if (ability == null) {
		continue;
	    }
	    free(ability);
	}
    }

    public void clear() {
	for (Pool<? extends Ability> pool : pools.values()) {
	    pool.clear();
	}
    }
}
