package com.lok.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class MapRevelationComponent implements Component, Poolable {
    public float minRevelationRadius = 0;
    public float maxRevelationRadius = 0;
    public float revelationRadius    = 0;
    public float incPerFrame	     = 0;

    @Override
    public void reset() {
	minRevelationRadius = 0;
	maxRevelationRadius = 0;
	revelationRadius = 0;
	incPerFrame = 0;
    }

}
