package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class MapRevelationComponent implements Component, Poolable {
    public int revelationRadius = 1;

    @Override
    public void reset() {
	revelationRadius = 1;
    }

}
