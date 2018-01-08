package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.lok.game.ecs.EntityEngine.EntityID;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.IDComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.map.Map;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;
import com.lok.game.map.Portal;

public class CollisionSystem extends IteratingSystem implements MapListener {
    public static interface CollisionListener {
	public void onEntityCollision(EntityID entityIDA, Entity entityA, EntityID entityIDB, Entity entityB);

	public void onPortalCollision(EntityID entityID, Entity entity, Portal portal);
    }

    private final ComponentMapper<CollisionComponent> collisionComponentMapper;
    private final ComponentMapper<IDComponent>	      idComponentMapper;
    private final Array<CollisionListener>	      collisionListeners;
    private Map					      map;

    public CollisionSystem(ComponentMapper<IDComponent> idComponentMapper, ComponentMapper<CollisionComponent> collisionComponentMapper) {
	super(Family.all(SizeComponent.class, CollisionComponent.class).get());

	this.collisionComponentMapper = collisionComponentMapper;
	this.idComponentMapper = idComponentMapper;
	this.map = null;
	this.collisionListeners = new Array<CollisionListener>();

	MapManager.getManager().addMapListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final CollisionComponent collisionComponent = collisionComponentMapper.get(entity);

	if (map != null) {
	    final IDComponent idComp = idComponentMapper.get(entity);

	    for (Portal portal : map.getPortals()) {
		if (portal.isColliding(collisionComponent.collisionRectangle)) {
		    for (CollisionListener collisionListener : collisionListeners) {
			collisionListener.onPortalCollision(idComp.entityID, entity, portal);
		    }
		}
	    }

	    for (Entity mapEntity : map.getEntities()) {
		if (entity.equals(mapEntity)) {
		    continue;
		}

		final CollisionComponent collisionComponentMapEntity = collisionComponentMapper.get(mapEntity);
		final IDComponent idCompMapEntity = idComponentMapper.get(mapEntity);

		if (collisionComponentMapEntity != null && collisionComponentMapEntity.collisionRectangle.overlaps(collisionComponent.collisionRectangle)) {
		    for (CollisionListener collisionListener : collisionListeners) {
			collisionListener.onEntityCollision(idComp.entityID, entity, idCompMapEntity.entityID, mapEntity);
		    }
		}
	    }
	}
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	this.map = map;
    }

    public void addCollisionListener(CollisionListener collisionListener) {
	collisionListeners.add(collisionListener);
    }

    public void removeCollisionListener(CollisionListener collisionListener) {
	collisionListeners.removeValue(collisionListener, false);
    }

}
