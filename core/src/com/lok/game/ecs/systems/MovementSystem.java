package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.PositionComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.ecs.components.SpeedComponent;

public class MovementSystem extends IteratingSystem {
    private final ComponentMapper<SpeedComponent>     speedComponentMapper;
    private final ComponentMapper<PositionComponent>  positionComponentMapper;
    private final ComponentMapper<CollisionComponent> collisionComponentMapper;
    private final ComponentMapper<SizeComponent>      sizeComponentMapper;

    public MovementSystem(ComponentMapper<PositionComponent> positionComponentMapper, ComponentMapper<SpeedComponent> speedComponentMapper,
	    ComponentMapper<CollisionComponent> collisionComponentMapper, ComponentMapper<SizeComponent> sizeComponentMapper) {
	super(Family.all(PositionComponent.class, SpeedComponent.class).get());

	this.positionComponentMapper = positionComponentMapper;
	this.speedComponentMapper = speedComponentMapper;
	this.collisionComponentMapper = collisionComponentMapper;
	this.sizeComponentMapper = sizeComponentMapper;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final PositionComponent positionComponent = positionComponentMapper.get(entity);
	final SpeedComponent speedComponent = speedComponentMapper.get(entity);
	final SizeComponent sizeComponent = sizeComponentMapper.get(entity);
	final CollisionComponent collisionComponent = collisionComponentMapper.get(entity);

	positionComponent.previousPosition.set(positionComponent.position);
	positionComponent.position.add(speedComponent.speed.x * deltaTime, speedComponent.speed.y * deltaTime);

	if (sizeComponent != null) {
	    sizeComponent.boundingRectangle.setPosition(positionComponent.position);
	}

	if (collisionComponent != null) {
	    collisionComponent.collisionRectangle.setPosition(positionComponent.position.x + collisionComponent.rectOffset.x,
		    positionComponent.position.y + collisionComponent.rectOffset.y);
	}
    }
}
