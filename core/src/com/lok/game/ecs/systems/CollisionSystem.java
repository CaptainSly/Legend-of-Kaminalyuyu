package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.components.CollisionComponent;
import com.lok.game.ecs.components.PositionComponent;

public class CollisionSystem extends IteratingSystem {
    private final ComponentMapper<PositionComponent>  positionComponentMapper;
    private final ComponentMapper<CollisionComponent> collisionComponentMapper;

    public CollisionSystem(ComponentMapper<PositionComponent> positionComponentMapper, ComponentMapper<CollisionComponent> collisionComponentMapper) {
	super(Family.all(PositionComponent.class, CollisionComponent.class).get());

	this.positionComponentMapper = positionComponentMapper;
	this.collisionComponentMapper = collisionComponentMapper;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final PositionComponent positionComponent = positionComponentMapper.get(entity);
	final CollisionComponent collisionComponent = collisionComponentMapper.get(entity);

	collisionComponent.collisionRectangle.setPosition(positionComponent.position.x + collisionComponent.rectOffset.x,
		positionComponent.position.y + collisionComponent.rectOffset.y);
	collisionComponent.boundingRectangle.setPosition(positionComponent.position);
    }

}
