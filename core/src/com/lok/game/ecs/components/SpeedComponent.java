package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class SpeedComponent implements Component, Poolable {
    public Vector2 speed    = new Vector2(0, 0);
    public float   maxSpeed = 0;

    @Override
    public void reset() {
	speed.set(0, 0);
	maxSpeed = 0;
    }
}
