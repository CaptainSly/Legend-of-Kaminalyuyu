package com.lok.game.ecs.components;

import com.badlogic.gdx.math.Circle;

public class MapRevelationComponent implements Component<MapRevelationComponent> {
    public float  minRevelationRadius = 0;
    public float  maxRevelationRadius = 0;
    public float  revelationRadius    = 0;
    public float  incPerFrame	      = 0;
    public Circle revelationCircle    = new Circle();

    @Override
    public void reset() {
	minRevelationRadius = 0;
	maxRevelationRadius = 0;
	revelationRadius = 0;
	incPerFrame = 0;
	revelationCircle.set(0, 0, 0);
    }

    @Override
    public void initialize(MapRevelationComponent configComponent) {
	this.minRevelationRadius = configComponent.minRevelationRadius;
	this.maxRevelationRadius = configComponent.maxRevelationRadius;
	this.revelationRadius = configComponent.revelationRadius;
	this.incPerFrame = configComponent.incPerFrame;
	this.revelationCircle.set(configComponent.revelationCircle);
    }

}
