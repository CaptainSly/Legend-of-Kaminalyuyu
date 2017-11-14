package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.map.Map;
import com.lok.game.map.Map.Portal;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;

public class CollisionSystem extends IteratingSystem implements MapListener {
    private final ComponentMapper<SizeComponent>      sizeComponentMapper;
    private final ComponentMapper<CollisionComponent> collisionComponentMapper;
    private final ComponentMapper<IDComponent>	      idComponentMapper;
    private Map					      map;

    public CollisionSystem(ComponentMapper<IDComponent> idComponentMapper, ComponentMapper<SizeComponent> sizeComponentMapper,
	    ComponentMapper<CollisionComponent> collisionComponentMapper) {
	super(Family.all(SizeComponent.class, CollisionComponent.class).get());

	this.sizeComponentMapper = sizeComponentMapper;
	this.collisionComponentMapper = collisionComponentMapper;
	this.idComponentMapper = idComponentMapper;
	this.map = null;

	MapManager.getManager().addListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final SizeComponent sizeComponent = sizeComponentMapper.get(entity);
	final CollisionComponent collisionComponent = collisionComponentMapper.get(entity);

	// TODO check for collisions and execute logic
	if (map != null) {
	    for (Portal portal : map.getPortals()) {
		if (portal.isColliding(collisionComponent.collisionRectangle)) {
		    if (idComponentMapper.get(entity).entityID == EntityID.PLAYER) {
			sizeComponent.previousPosition.set(portal.getTargetPosition());
			sizeComponent.boundingRectangle.setPosition(portal.getTargetPosition());
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
