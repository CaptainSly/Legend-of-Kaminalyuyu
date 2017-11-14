package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.lok.game.ecs.EntityEngine.EntityID;

public class IDComponent implements Component, Poolable {
    public EntityID entityID = null;

    @Override
    public void reset() {
	entityID = null;
    }
}
