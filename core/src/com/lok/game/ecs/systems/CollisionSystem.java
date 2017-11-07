package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.PositionComponent;
import com.lok.game.map.Map;
import com.lok.game.map.Map.Portal;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;

public class CollisionSystem extends IteratingSystem implements MapListener {
    private final ComponentMapper<PositionComponent>  positionComponentMapper;
    private final ComponentMapper<CollisionComponent> collisionComponentMapper;
    private Map					      map;

    public CollisionSystem(ComponentMapper<PositionComponent> positionComponentMapper, ComponentMapper<CollisionComponent> collisionComponentMapper) {
	super(Family.all(PositionComponent.class, CollisionComponent.class).get());

	this.positionComponentMapper = positionComponentMapper;
	this.collisionComponentMapper = collisionComponentMapper;
	this.map = null;

	MapManager.getManager().addListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final PositionComponent positionComponent = positionComponentMapper.get(entity);
	final CollisionComponent collisionComponent = collisionComponentMapper.get(entity);

	// TODO check for collisions and execute logic
	if (map != null) {
	    for (Portal portal : map.getPortals()) {
		if (portal.isColliding(collisionComponent.collisionRectangle)) {
		    if (entity.flags == EntityID.PLAYER.ordinal()) {
			positionComponent.previousPosition.set(portal.getTargetPosition());
			positionComponent.position.set(portal.getTargetPosition());
		    }
		}
	    }
	}
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	this.map = map;
    }

}
