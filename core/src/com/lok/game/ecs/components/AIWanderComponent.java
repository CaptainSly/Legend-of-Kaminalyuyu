package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class AIWanderComponent implements Component, Poolable {
    public float wanderTime = 0.0f;

    @Override
    public void reset() {
	wanderTime = 0.0f;
    }
}
