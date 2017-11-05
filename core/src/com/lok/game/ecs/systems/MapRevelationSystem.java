package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.MapRevelationComponent;
import com.lok.game.ecs.components.PositionComponent;
import com.lok.game.map.Map;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;

public class MapRevelationSystem extends IteratingSystem implements MapListener {
    private final ComponentMapper<PositionComponent>	  positionComponentMapper;
    private final ComponentMapper<CollisionComponent>	  collisionComponentMapper;
    private final ComponentMapper<MapRevelationComponent> mapRevelationComponentMapper;
    private Map						  map;

    public MapRevelationSystem(ComponentMapper<PositionComponent> positionComponentMapper, ComponentMapper<CollisionComponent> collisionComponentMapper,
	    ComponentMapper<MapRevelationComponent> mapRevelationComponentMapper) {
	super(Family.all(MapRevelationComponent.class, PositionComponent.class, CollisionComponent.class).get());

	this.positionComponentMapper = positionComponentMapper;
	this.collisionComponentMapper = collisionComponentMapper;
	this.mapRevelationComponentMapper = mapRevelationComponentMapper;
	map = null;

	MapManager.getManager().addListener(this);
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	this.map = map;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	if (map != null) {
	    final PositionComponent positionComponent = positionComponentMapper.get(entity);
	    if (!positionComponent.previousPosition.equals(positionComponent.position)) {
		final MapRevelationComponent mapRevelationComponent = mapRevelationComponentMapper.get(entity);
		final CollisionComponent collisionComponent = collisionComponentMapper.get(entity);

		map.revealArea(collisionComponent.boundingRectangle, mapRevelationComponent.revelationRadius);
	    }
	}
    }

}
