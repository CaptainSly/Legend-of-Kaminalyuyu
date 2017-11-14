package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.SizeComponent;
import com.lok.game.ecs.components.SpeedComponent;

public class MovementSystem extends IteratingSystem {
    private final ComponentMapper<SpeedComponent>     speedComponentMapper;
    private final ComponentMapper<CollisionComponent> collisionComponentMapper;
    private final ComponentMapper<SizeComponent>      sizeComponentMapper;

    public MovementSystem(ComponentMapper<SpeedComponent> speedComponentMapper, ComponentMapper<CollisionComponent> collisionComponentMapper,
	    ComponentMapper<SizeComponent> sizeComponentMapper) {
	super(Family.all(SizeComponent.class, SpeedComponent.class).get());

	this.speedComponentMapper = speedComponentMapper;
	this.collisionComponentMapper = collisionComponentMapper;
	this.sizeComponentMapper = sizeComponentMapper;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final SpeedComponent speedComponent = speedComponentMapper.get(entity);
	final SizeComponent sizeComponent = sizeComponentMapper.get(entity);
	final CollisionComponent collisionComponent = collisionComponentMapper.get(entity);

	sizeComponent.boundingRectangle.x += speedComponent.speed.x * deltaTime;
	sizeComponent.boundingRectangle.y += speedComponent.speed.y * deltaTime;

	if (collisionComponent != null) {
	    collisionComponent.collisionRectangle.setPosition(sizeComponent.boundingRectangle.x + collisionComponent.rectOffset.x,
		    sizeComponent.boundingRectangle.y + collisionComponent.rectOffset.y);
	}
    }
}
