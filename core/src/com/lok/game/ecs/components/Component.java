package com.lok.game.ecs.components;

import com.badlogic.gdx.utils.Pool.Poolable;

public interface Component<T> extends com.badlogic.ashley.core.Component, Poolable {
    public void initialize(T configComponent);
}
