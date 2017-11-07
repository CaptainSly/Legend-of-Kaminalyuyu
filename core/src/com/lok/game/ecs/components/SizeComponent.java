package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool.Poolable;

public class SizeComponent implements Component, Poolable {
    public Rectangle boundingRectangle = new Rectangle(0, 0, 0, 0);

    @Override
    public void reset() {
	boundingRectangle.set(0, 0, 0, 0);
    }

}
