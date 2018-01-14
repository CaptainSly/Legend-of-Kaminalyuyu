package com.lok.game.ecs.components;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CollisionComponent implements Component<CollisionComponent> {
    public Vector2   rectOffset		= new Vector2(0, 0);
    public Rectangle collisionRectangle	= new Rectangle(0, 0, 0, 0);

    @Override
    public void reset() {
	rectOffset.set(0, 0);
	collisionRectangle.set(0, 0, 0, 0);
    }

    @Override
    public void initialize(CollisionComponent configComponent) {
	this.rectOffset.set(configComponent.rectOffset);
	this.collisionRectangle.set(configComponent.collisionRectangle);
    }
}
