package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.components.MapRevelationComponent;
import com.lok.game.ecs.components.SizeComponent;

public class MapRevelationSystem extends IteratingSystem {
    private final ComponentMapper<SizeComponent>	  sizeComponentMapper;
    private final ComponentMapper<MapRevelationComponent> mapRevelationComponentMapper;

    public MapRevelationSystem(ComponentMapper<SizeComponent> sizeComponentMapper, ComponentMapper<MapRevelationComponent> mapRevelationComponentMapper) {
	super(Family.all(MapRevelationComponent.class, SizeComponent.class).get());

	this.sizeComponentMapper = sizeComponentMapper;
	this.mapRevelationComponentMapper = mapRevelationComponentMapper;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final MapRevelationComponent mapRevelationComponent = mapRevelationComponentMapper.get(entity);
	final SizeComponent sizeComponent = sizeComponentMapper.get(entity);

	mapRevelationComponent.revelationRadius += mapRevelationComponent.incPerFrame * deltaTime;
	if (mapRevelationComponent.revelationRadius > mapRevelationComponent.maxRevelationRadius) {
	    mapRevelationComponent.revelationRadius = mapRevelationComponent.maxRevelationRadius;
	    mapRevelationComponent.incPerFrame = -mapRevelationComponent.incPerFrame;
	} else if (mapRevelationComponent.revelationRadius < mapRevelationComponent.minRevelationRadius) {
	    mapRevelationComponent.revelationRadius = mapRevelationComponent.minRevelationRadius;
	    mapRevelationComponent.incPerFrame = -mapRevelationComponent.incPerFrame;
	}

	mapRevelationComponent.revelationCircle.set(sizeComponent.interpolatedPosition.x + sizeComponent.boundingRectangle.width * 0.5f,
		sizeComponent.interpolatedPosition.y + sizeComponent.boundingRectangle.height * 0.5f, mapRevelationComponent.revelationRadius);
    }

}
