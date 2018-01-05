package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.ecs.components.SpeedComponent;
import com.lok.game.map.Map;
import com.lok.game.map.MapListener;
import com.lok.game.map.MapManager;

public class MovementSystem extends IteratingSystem implements MapListener {
    private final ComponentMapper<SpeedComponent>     speedComponentMapper;
    private final ComponentMapper<CollisionComponent> collisionComponentMapper;
    private final ComponentMapper<SizeComponent>      sizeComponentMapper;
    private Map					      map;

    public MovementSystem(ComponentMapper<SpeedComponent> speedComponentMapper, ComponentMapper<CollisionComponent> collisionComponentMapper,
	    ComponentMapper<SizeComponent> sizeComponentMapper) {
	super(Family.all(SizeComponent.class, SpeedComponent.class, CollisionComponent.class).get());

	this.speedComponentMapper = speedComponentMapper;
	this.collisionComponentMapper = collisionComponentMapper;
	this.sizeComponentMapper = sizeComponentMapper;
	this.map = null;

	MapManager.getManager().addMapListener(this);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final SpeedComponent speedComponent = speedComponentMapper.get(entity);

	if (speedComponent.speed.equals(Vector2.Zero)) {
	    return;
	}

	final SizeComponent sizeComponent = sizeComponentMapper.get(entity);
	final CollisionComponent collisionComponent = collisionComponentMapper.get(entity);
	final float currentX = sizeComponent.boundingRectangle.x;
	final float currentY = sizeComponent.boundingRectangle.y;

	sizeComponent.boundingRectangle.x += speedComponent.speed.x * deltaTime;
	sizeComponent.boundingRectangle.y += speedComponent.speed.y * deltaTime;

	if (collisionComponent != null) {
	    collisionComponent.collisionRectangle.setPosition(sizeComponent.boundingRectangle.x + collisionComponent.rectOffset.x,
		    sizeComponent.boundingRectangle.y + collisionComponent.rectOffset.y);

	    if (map != null && !map.isPathable(collisionComponent.collisionRectangle)) {
		sizeComponent.boundingRectangle.x = currentX;
		sizeComponent.boundingRectangle.y = currentY;
		collisionComponent.collisionRectangle.x = currentX + collisionComponent.rectOffset.x;
		collisionComponent.collisionRectangle.y = currentY + collisionComponent.rectOffset.y;
	    }
	}
    }

    @Override
    public void onMapChange(MapManager manager, Map map) {
	this.map = map;
    }
}
