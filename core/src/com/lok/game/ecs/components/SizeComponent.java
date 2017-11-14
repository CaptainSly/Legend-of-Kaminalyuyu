package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class SizeComponent implements Component, Poolable {
    public Vector2   previousPosition  = new Vector2(0, 0);
    public Rectangle boundingRectangle = new Rectangle(0, 0, 0, 0);

    @Override
    public void reset() {
	previousPosition.set(0, 0);
	boundingRectangle.set(0, 0, 0, 0);
    }

}
