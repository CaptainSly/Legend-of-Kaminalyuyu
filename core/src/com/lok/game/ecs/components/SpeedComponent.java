package com.lok.game.ecs.components;

import com.badlogic.gdx.math.Vector2;

public class SpeedComponent implements Component<SpeedComponent> {
    public Vector2 speed    = new Vector2(0, 0);
    public float   maxSpeed = 0;

    @Override
    public void reset() {
	speed.set(0, 0);
	maxSpeed = 0;
    }

    @Override
    public void initialize(SpeedComponent configComponent) {
	this.speed.set(configComponent.speed);
	this.maxSpeed = configComponent.maxSpeed;
    }
}
