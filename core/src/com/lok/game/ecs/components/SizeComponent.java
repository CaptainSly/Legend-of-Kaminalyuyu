package com.lok.game.ecs.components;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SizeComponent implements Component<SizeComponent> {
    public Vector2   interpolatedPosition = new Vector2(0, 0);
    public Rectangle boundingRectangle	  = new Rectangle(0, 0, 0, 0);

    @Override
    public void reset() {
	interpolatedPosition.set(0, 0);
	boundingRectangle.set(0, 0, 0, 0);
    }

    @Override
    public void initialize(SizeComponent configComponent) {
	this.interpolatedPosition.set(configComponent.interpolatedPosition);
	this.boundingRectangle.set(configComponent.boundingRectangle);
    }

}
