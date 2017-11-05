package com.lok.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.lok.game.ecs.components.PositionComponent;
import com.lok.game.ecs.components.SpeedComponent;

public class MovementSystem extends IteratingSystem {
    private final ComponentMapper<SpeedComponent>    speedComponentMapper;
    private final ComponentMapper<PositionComponent> positionComponentMapper;

    public MovementSystem(ComponentMapper<PositionComponent> positionComponentMapper, ComponentMapper<SpeedComponent> speedComponentMapper) {
	super(Family.all(PositionComponent.class, SpeedComponent.class).get());

	this.positionComponentMapper = positionComponentMapper;
	this.speedComponentMapper = speedComponentMapper;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
	final PositionComponent positionComponent = positionComponentMapper.get(entity);
	final SpeedComponent speedComponent = speedComponentMapper.get(entity);

	positionComponent.previousPosition.set(positionComponent.position);
	positionComponent.position.add(speedComponent.speed.x * deltaTime, speedComponent.speed.y * deltaTime);
    }
}
