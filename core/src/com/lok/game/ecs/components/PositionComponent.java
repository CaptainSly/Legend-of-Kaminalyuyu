package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class PositionComponent implements Component, Poolable {
    public Vector2 position	    = new Vector2(0, 0);
    public Vector2 previousPosition = new Vector2(0, 0);

    @Override
    public void reset() {
	position.set(0, 0);
	previousPosition.set(0, 0);
    }
}
