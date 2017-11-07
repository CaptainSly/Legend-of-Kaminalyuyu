package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class CollisionComponent implements Component, Poolable {
    public Vector2   rectOffset		= new Vector2(0, 0);
    public Rectangle collisionRectangle	= new Rectangle(0, 0, 0, 0);

    @Override
    public void reset() {
	rectOffset.set(0, 0);
	collisionRectangle.set(0, 0, 0, 0);
    }
}
